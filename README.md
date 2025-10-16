# Stylus Note App

OPPO/OnePlus スタイラスAPI対応のシンプルなノートアプリ

## 📱 機能

- ✅ 筆圧対応の描画
- ✅ OPPO/OnePlusデバイスでモーション予測（低遅延）
- ✅ 色選択（Black, Red, Blue, Green）
- ✅ ペンの太さ調整（Thin, Medium, Thick）
- ✅ Undo機能
- ✅ Canvas消去

## 🚀 ビルド方法

### ローカルビルド

```bash
# プロジェクトディレクトリに移動
cd sample-note-app

# Debug APKビルド
./gradlew assembleDebug

# Release APKビルド
./gradlew assembleRelease
```

### GitHub Actionsでビルド

1. このリポジトリをGitHubにプッシュ
2. GitHub Actionsが自動的に実行される
3. Actionsタブから成果物（APK）をダウンロード

## 📦 APKの場所

- **Debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 🎯 対応デバイス

### 完全対応（モーション予測有効）
- OPPO デバイス
- OnePlus デバイス

### 基本対応（標準タッチ）
- その他すべてのAndroidデバイス（API 26+）

## 🔧 技術詳細

### 使用技術
- **言語**: Java
- **最小SDK**: 26 (Android 8.0)
- **ターゲットSDK**: 34 (Android 14)
- **ビルドツール**: Gradle 8.2
- **CI/CD**: GitHub Actions

### OPPO/OnePlus API
- **MotionPredictor**: タッチポイント予測
- **TouchPointInfo**: 座標、筆圧、傾き情報
- **NativeForecast**: JNIネイティブライブラリ

### 主要クラス
- `MainActivity.java` - メインアクティビティ
- `DrawingView.java` - カスタム描画ビュー（筆圧対応）
- `MotionPredictor.java` - OPPO予測API

## 📋 必要な権限

なし（ネットワーク不要、完全オフライン動作）

## 🛠️ カスタマイズ

### 色を追加

`MainActivity.java`:
```java
drawingView.setColor(Color.YELLOW);
```

### ペンの太さを変更

`MainActivity.java`:
```java
drawingView.setStrokeWidth(15f);
```

### 背景色を変更

`activity_main.xml`:
```xml
android:background="@color/your_color"
```

## 📊 プロジェクト構造

```
sample-note-app/
├── .github/
│   └── workflows/
│       └── android-build.yml        # GitHub Actions設定
├── app/
│   ├── src/main/
│   │   ├── java/
│   │   │   ├── com/oplusos/vfxsdk/forecast/
│   │   │   │   ├── TouchPointInfo.java
│   │   │   │   ├── NativeForecast.java
│   │   │   │   └── MotionPredictor.java
│   │   │   └── com/example/stylus/noteapp/
│   │   │       ├── MainActivity.java
│   │   │       └── DrawingView.java
│   │   ├── jniLibs/
│   │   │   └── arm64-v8a/
│   │   │       └── libforecast.so
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── menu/
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## 🔍 トラブルシューティング

### ビルドエラー

**問題**: `UnsatisfiedLinkError`
**解決**: jniLibs/arm64-v8a/libforecast.so が正しく配置されているか確認

**問題**: Gradle sync失敗
**解決**: `./gradlew clean` を実行してから再ビルド

### GitHub Actions失敗

**問題**: gradlewの権限エラー
**解決**: `chmod +x gradlew` を実行してコミット

## 📄 ライセンス

このサンプルアプリは学習・研究目的で作成されました。

**注意**: OPPO/OnePlusのネイティブライブラリ（libforecast.so）は、商用利用の場合OPPOから正式なSDKを入手してください。

## 🤝 貢献

プルリクエスト歓迎！

## 📞 サポート

問題が発生した場合は、Issuesで報告してください。

---

**バージョン**: 1.0  
**ビルド**: GitHub Actions  
**作成日**: 2025年10月16日

