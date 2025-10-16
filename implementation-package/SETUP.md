# セットアップガイド - 詳細手順

## 目次

1. [前提条件](#前提条件)
2. [ステップ1: プロジェクト準備](#ステップ1-プロジェクト準備)
3. [ステップ2: ネイティブライブラリの配置](#ステップ2-ネイティブライブラリの配置)
4. [ステップ3: Javaコードの統合](#ステップ3-javaコードの統合)
5. [ステップ4: リソースファイルの追加](#ステップ4-リソースファイルの追加)
6. [ステップ5: マニフェストの設定](#ステップ5-マニフェストの設定)
7. [ステップ6: 依存関係の追加](#ステップ6-依存関係の追加)
8. [ステップ7: 実装とテスト](#ステップ7-実装とテスト)

---

## 前提条件

### 必要な環境

- **Android Studio**: Arctic Fox (2020.3.1) 以降
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Gradle**: 7.0 以降
- **Kotlin** (オプション): 1.8.0 以降

### 必要な知識

- Android開発の基礎
- Java/Kotlinの基本文法
- MotionEventの理解

---

## ステップ1: プロジェクト準備

### 1.1 新規プロジェクト作成

Android Studioで新規プロジェクトを作成：

```
File -> New -> New Project
→ Empty Activity を選択
→ Name: DrawApp (任意)
→ Package name: com.example.drawapp (任意)
→ Minimum SDK: API 26
```

### 1.2 プロジェクト構造の確認

```
your-project/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── jniLibs/          # ← ネイティブライブラリ配置先
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── ...
│   └── build.gradle
└── build.gradle
```

---

## ステップ2: ネイティブライブラリの配置

### 2.1 jniLibsディレクトリの作成

```bash
mkdir -p app/src/main/jniLibs/arm64-v8a
mkdir -p app/src/main/jniLibs/armeabi-v7a
```

または Android Studio で:
```
右クリック app/src/main -> New -> Directory -> jniLibs
```

### 2.2 OPPO/OnePlusライブラリのコピー

```bash
# このパッケージから
cp implementation-package/libs/oppo-forecast/arm64-v8a/libforecast.so \
   your-project/app/src/main/jniLibs/arm64-v8a/

# 32bit対応が必要な場合
cp implementation-package/libs/oppo-forecast/armeabi-v7a/libforecast.so \
   your-project/app/src/main/jniLibs/armeabi-v7a/
```

### 2.3 ライブラリの確認

配置後のディレクトリ構造：
```
jniLibs/
├── arm64-v8a/
│   └── libforecast.so
└── armeabi-v7a/
    └── libforecast.so
```

---

## ステップ3: Javaコードの統合

### 3.1 パッケージ構造の作成

```
com.example.drawapp/
├── stylus/           # スタイラスハンドラー
├── view/             # カスタムビュー
└── MainActivity.java
```

### 3.2 必須クラスのコピー

以下のファイルを順番にコピー：

#### 1. データクラス

```bash
# TouchPointInfo.java (OPPO用)
cp implementation-package/java/interfaces/TouchPointInfo.java \
   your-project/app/src/main/java/com/example/drawapp/interfaces/
```

内容を作成：

```java
package com.example.drawapp.interfaces;

public class TouchPointInfo {
    public float x;
    public float y;
    public float pressure;
    public float axisTilt;
    public long timestamp;
    
    public TouchPointInfo(float x, float y, float pressure, float axisTilt, long timestamp) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.axisTilt = axisTilt;
        this.timestamp = timestamp;
    }
    
    public TouchPointInfo() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.pressure = 0.0f;
        this.axisTilt = 0.0f;
        this.timestamp = 0L;
    }
}
```

#### 2. ネイティブインターフェース

```java
package com.example.drawapp.interfaces;

public class NativeForecast {
    public static native long create(long handle);
    public static native void destroy(long handle);
    public static native void pushTouchPoint(long handle, TouchPointInfo info);
    public static native TouchPointInfo predictTouchPoint(long handle);
    public static native void reset(long handle);
    public static native void setRefreshRate(long handle, float rate);
    public static native void setDpi(long handle, float dpiX, float dpiY);
    public static native void setMaxPredictTime(long handle, float maxTime);
    
    static {
        System.loadLibrary("forecast");
    }
}
```

#### 3. スタイラスハンドラー

```bash
# OppoStylusHandler.java
cp implementation-package/java/stylus/OppoStylusHandler.java \
   your-project/app/src/main/java/com/example/drawapp/stylus/
```

パッケージ名を変更：
```java
package com.example.drawapp.stylus;  // あなたのパッケージ名に変更
```

#### 4. 統合マネージャー

```bash
# UnifiedStylusManager.java
cp implementation-package/java/stylus/UnifiedStylusManager.java \
   your-project/app/src/main/java/com/example/drawapp/stylus/
```

#### 5. DrawView

```bash
# UnifiedDrawView.java
cp implementation-package/java/view/UnifiedDrawView.java \
   your-project/app/src/main/java/com/example/drawapp/view/
```

---

## ステップ4: リソースファイルの追加

### 4.1 レイアウトファイル

`res/layout/activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="?attr/colorPrimary"/>
    </com.google.android.material.appbar.AppBarLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <TextView
            android:id="@+id/status_text"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="8dp"
            android:text="Initializing..."
            android:background="#F5F5F5"/>

        <com.example.drawapp.view.UnifiedDrawView
            android:id="@+id/draw_view"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:background="#FFFFFF"/>
    </LinearLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_clear"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        app:srcCompat="@android:drawable/ic_menu_delete"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 4.2 メニューファイル

`res/menu/menu_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <item
        android:id="@+id/action_undo"
        android:title="Undo"
        app:showAsAction="ifRoom"/>
    
    <item
        android:id="@+id/action_clear"
        android:title="Clear"
        app:showAsAction="never"/>
</menu>
```

### 4.3 文字列リソース

`res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">DrawApp</string>
    <string name="stylus_connected">Stylus Connected</string>
    <string name="stylus_disconnected">Stylus Disconnected</string>
</resources>
```

---

## ステップ5: マニフェストの設定

`AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.drawapp">

    <!-- 必要な権限 -->
    <!-- SonarPen用 -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    
    <!-- Samsung S Pen用 -->
    <uses-permission android:name="com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
        
        <activity
            android:name=".MainActivity"
            android:screenOrientation="portrait"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

---

## ステップ6: 依存関係の追加

### 6.1 プロジェクトレベル build.gradle

`build.gradle` (プロジェクトルート):

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

### 6.2 アプリレベル build.gradle

`app/build.gradle`:

```gradle
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.example.drawapp'
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.drawapp"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0"
        
        // ABIフィルター（必要に応じて調整）
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a'
        }
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.coordinatorlayout:coordinatorlayout:1.2.0'
}
```

---

## ステップ7: 実装とテスト

### 7.1 MainActivity の実装

```java
package com.example.drawapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.drawapp.stylus.UnifiedStylusManager;
import com.example.drawapp.view.UnifiedDrawView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 1;
    
    private UnifiedDrawView drawView;
    private UnifiedStylusManager stylusManager;
    private TextView statusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // ビュー初期化
        drawView = findViewById(R.id.draw_view);
        statusText = findViewById(R.id.status_text);
        
        FloatingActionButton fab = findViewById(R.id.fab_clear);
        fab.setOnClickListener(v -> {
            drawView.clear();
            Toast.makeText(this, "Canvas cleared", Toast.LENGTH_SHORT).show();
        });
        
        // 権限チェック
        if (checkPermissions()) {
            initializeStylusManager();
        } else {
            requestPermissions();
        }
    }
    
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, 
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            REQUEST_RECORD_AUDIO);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeStylusManager();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void initializeStylusManager() {
        stylusManager = new UnifiedStylusManager(this,
            new UnifiedStylusManager.StylusEventCallback() {
                @Override
                public void onPredictedPoint(float x, float y, float pressure, float tilt) {
                    drawView.onPredictedPoint(x, y, pressure, tilt);
                }
                
                @Override
                public void onButtonPressed() {
                    Toast.makeText(MainActivity.this, "Button Pressed", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onButtonReleased() {}
                
                @Override
                public void onAirMotion(float deltaX, float deltaY) {}
                
                @Override
                public void onConnectionChanged(boolean connected) {
                    runOnUiThread(() -> {
                        statusText.setText(connected ? "Connected" : "Disconnected");
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error: " + message, 
                            Toast.LENGTH_LONG).show();
                    });
                }
            });
        
        boolean success = stylusManager.initialize(this);
        
        if (success) {
            drawView.setStylusManager(stylusManager);
            String vendor = stylusManager.getVendorType().toString();
            statusText.setText("Stylus API: " + vendor);
            Toast.makeText(this, "Using " + vendor + " API", Toast.LENGTH_LONG).show();
        } else {
            statusText.setText("Stylus: Not Available");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stylusManager != null) {
            stylusManager.destroy();
        }
    }
}
```

### 7.2 ビルドと実行

1. **Sync Gradle**: `File -> Sync Project with Gradle Files`
2. **Clean Build**: `Build -> Clean Project`
3. **Build**: `Build -> Make Project`
4. **Run**: デバイスまたはエミュレータで実行

### 7.3 動作確認

✅ アプリが起動する
✅ ステータステキストにデバイスタイプが表示される
✅ スタイラスで描画できる
✅ FABで画面クリアできる
✅ 筆圧が反映される（対応デバイスの場合）

---

## トラブルシューティング

### ビルドエラー

**エラー**: `Cannot resolve symbol 'TouchPointInfo'`

**解決策**: パッケージ名を確認し、import文を追加
```java
import com.example.drawapp.interfaces.TouchPointInfo;
```

### ライブラリエラー

**エラー**: `UnsatisfiedLinkError: dlopen failed`

**解決策**:
1. `jniLibs` フォルダの配置を確認
2. ABIフィルターを確認
3. ライブラリファイルが破損していないか確認

### 実行時エラー

**エラー**: `ClassNotFoundException`

**解決策**: パッケージ名がマニフェストと一致しているか確認

---

## 次のステップ

1. [実装ガイド](../docs/implementation-guide.md) で詳細な機能を実装
2. [クイックリファレンス](../docs/quick-reference.md) でコード例を参照
3. カスタマイズと機能追加

---

**セットアップガイド v1.0**

