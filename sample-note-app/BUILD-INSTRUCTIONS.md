# ビルド手順書

## 🏗️ ビルド方法（3つの選択肢）

### 方法1: GitHub Actions（推奨）⭐

#### 準備
```bash
# リポジトリを作成してプッシュ
cd sample-note-app
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/stylus-note-app.git
git push -u origin main
```

#### ビルド実行
1. GitHubリポジトリページに移動
2. `Actions` タブをクリック
3. 自動的にビルドが開始
4. 完了を待つ（約5-10分）

#### APKダウンロード
1. 完了したワークフローをクリック
2. `Artifacts` セクションまでスクロール
3. `app-debug` または `app-release` をクリックしてダウンロード

### 方法2: Android Studio

#### 準備
1. Android Studioを起動
2. `Open Project` で `sample-note-app` フォルダを選択
3. Gradle syncを待つ

#### ビルド実行
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

または

```
Build → Generate Signed Bundle / APK → APK
```

#### APK場所
```
app/build/outputs/apk/debug/app-debug.apk
```

### 方法3: コマンドライン

#### Windows
```bash
cd sample-note-app
gradlew.bat assembleDebug
```

#### Mac/Linux
```bash
cd sample-note-app
./gradlew assembleDebug
```

#### Release版
```bash
# Windows
gradlew.bat assembleRelease

# Mac/Linux
./gradlew assembleRelease
```

---

## 🔧 ビルド前の準備

### 1. JDK確認

```bash
java -version
# Java 17以上が必要
```

JDKがない場合:
- Windows: https://adoptium.net/
- Mac: `brew install openjdk@17`
- Linux: `sudo apt install openjdk-17-jdk`

### 2. Android SDK設定

`local.properties` を作成:
```properties
sdk.dir=C:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
```

### 3. Gradle Wrapper準備

```bash
# gradle-wrapper.jarを生成
gradle wrapper --gradle-version 8.2
```

または、Android Studioでプロジェクトを開くと自動生成されます。

---

## 📦 ビルド成果物

### Debug APK
```
ファイル名: app-debug.apk
場所: app/build/outputs/apk/debug/
署名: デバッグ署名（自動）
用途: 開発・テスト
```

### Release APK (未署名)
```
ファイル名: app-release-unsigned.apk
場所: app/build/outputs/apk/release/
署名: なし
用途: 署名後に本番デプロイ
```

---

## 🔐 署名付きAPKの作成

### ステップ1: キーストア生成

```bash
keytool -genkey -v -keystore my-release-key.keystore \
  -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

### ステップ2: app/build.gradleに追加

```gradle
android {
    signingConfigs {
        release {
            storeFile file('my-release-key.keystore')
            storePassword 'your_store_password'
            keyAlias 'my-key-alias'
            keyPassword 'your_key_password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### ステップ3: ビルド

```bash
./gradlew assembleRelease
```

成果物:
```
app/build/outputs/apk/release/app-release.apk (署名済み)
```

---

## 🤖 GitHub Actions設定

### 署名付きビルド（GitHub Secrets使用）

#### 1. キーストアをBase64エンコード

```bash
# Mac/Linux
base64 my-release-key.keystore > keystore.base64

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.keystore")) > keystore.base64
```

#### 2. GitHub Secretsに追加

リポジトリ → Settings → Secrets and variables → Actions

追加するSecret:
- `KEYSTORE_BASE64`: keystore.base64の内容
- `KEYSTORE_PASSWORD`: キーストアパスワード
- `KEY_ALIAS`: キーエイリアス
- `KEY_PASSWORD`: キーパスワード

#### 3. ワークフロー更新

`.github/workflows/android-build.yml`:
```yaml
- name: Decode Keystore
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/my-release-key.keystore

- name: Build Signed APK
  env:
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew assembleRelease
```

---

## 📊 ビルド時間の目安

### GitHub Actions
- 初回ビルド: 8-12分（依存関係ダウンロード含む）
- 2回目以降: 3-5分（キャッシュ有効）

### ローカル
- 初回ビルド: 5-8分
- 2回目以降: 30秒-2分

---

## 🔍 トラブルシューティング

### Gradle sync失敗

```bash
# キャッシュクリア
./gradlew clean

# 依存関係再ダウンロード
./gradlew --refresh-dependencies
```

### ビルドエラー: AAPT2

```gradle
// app/build.gradle
android {
    buildFeatures {
        aapt2 = true
    }
}
```

### メモリ不足

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4096m
```

### GitHub Actions タイムアウト

```yaml
# .github/workflows/android-build.yml
jobs:
  build:
    timeout-minutes: 30
```

---

## 📈 ビルド最適化

### 1. Gradleキャッシュ

`.github/workflows/android-build.yml` にすでに設定済み:
```yaml
- uses: actions/setup-java@v4
  with:
    cache: gradle
```

### 2. 並列ビルド

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.caching=true
```

### 3. ビルドスキャン

```bash
./gradlew assembleDebug --scan
```

---

## 📱 APKインストール

### ADBでインストール

```bash
# デバイスを接続
adb devices

# APKインストール
adb install app/build/outputs/apk/debug/app-debug.apk

# 既存アプリを上書き
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 直接インストール

1. APKをデバイスに転送
2. ファイルマネージャーで開く
3. 「インストール」をタップ
4. 「提供元不明のアプリ」を許可（必要な場合）

---

## 🎯 ビルド成功の確認

### チェックリスト

- [ ] Gradle syncが成功
- [ ] ビルドエラーなし
- [ ] APKファイルが生成される
- [ ] APKサイズが妥当（約2-5MB）
- [ ] インストールできる
- [ ] アプリが起動する
- [ ] 描画機能が動作する

---

## 📞 サポート

ビルドに問題がある場合：
1. [README.md](README.md) を確認
2. [DEPLOYMENT.md](DEPLOYMENT.md) を参照
3. GitHubでIssueを作成

---

**ビルド手順書 v1.0**  
**最終更新**: 2025年10月16日

