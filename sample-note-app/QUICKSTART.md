# クイックスタートガイド

## 🚀 5分で始める

### ステップ1: GitHubにアップロード

```bash
# sample-note-appディレクトリに移動
cd sample-note-app

# Gitリポジトリを初期化
git init

# すべてのファイルを追加
git add .

# コミット
git commit -m "Initial commit: Stylus Note App"

# GitHubリポジトリを作成してリモートを追加
git remote add origin https://github.com/YOUR_USERNAME/stylus-note-app.git

# プッシュ
git push -u origin main
```

### ステップ2: GitHub Actionsで自動ビルド

プッシュすると自動的に：
1. GitHub Actionsが起動
2. APKがビルドされる
3. Artifactsとしてダウンロード可能になる

### ステップ3: APKをダウンロード

1. GitHubリポジトリ → `Actions` タブ
2. 完了したワークフローをクリック
3. `Artifacts` セクションから以下をダウンロード：
   - `app-debug.apk` - デバッグ版
   - `app-release-unsigned.apk` - リリース版（未署名）

## 📱 インストール

### デバイスにインストール

```bash
# USB接続してADBでインストール
adb install app-debug.apk

# または、APKをデバイスに転送してタップ
```

## ✨ アプリの使い方

1. **描画**: 画面をタッチして描く
2. **色変更**: メニュー → Color → 色選択
3. **ペンの太さ**: メニュー → Stroke Width
4. **Undo**: ツールバーの戻るアイコン
5. **消去**: 右下のFABボタン

## 🔧 ローカルでビルド

Android Studioがある場合：

```bash
# Debug APK
./gradlew assembleDebug

# APKの場所
app/build/outputs/apk/debug/app-debug.apk
```

## 📊 対応状況

- ✅ OPPO/OnePlus: モーション予測で超低遅延
- ✅ その他Android: 標準タッチで動作
- ✅ 最小SDK: Android 8.0 (API 26)

---

所要時間: 約5分

