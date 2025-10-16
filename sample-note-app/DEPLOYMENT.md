# デプロイメントガイド

## GitHub Actionsでビルドする手順

### 1. リポジトリの準備

```bash
# 新しいGitHubリポジトリを作成
# リポジトリ名: stylus-note-app（任意）

# sample-note-appディレクトリに移動
cd sample-note-app

# Gitリポジトリを初期化
git init

# すべてのファイルを追加
git add .

# コミット
git commit -m "Initial commit: Stylus Note App with OPPO API"

# GitHubリポジトリをリモートとして追加
git remote add origin https://github.com/YOUR_USERNAME/stylus-note-app.git

# プッシュ
git push -u origin main
```

### 2. GitHub Actionsの自動実行

プッシュ後、自動的にGitHub Actionsが実行されます：

1. GitHubリポジトリページに移動
2. 「Actions」タブをクリック
3. 「Android CI」ワークフローを確認
4. ビルドの進行状況を確認

### 3. APKのダウンロード

ビルド完了後：

1. 完了したワークフローをクリック
2. 「Artifacts」セクションを確認
3. `app-debug` または `app-release` をダウンロード

## ローカルビルド

### 前提条件

- Android Studio インストール済み
- JDK 17 インストール済み
- Android SDK インストール済み

### ビルド手順

```bash
# プロジェクトディレクトリに移動
cd sample-note-app

# local.propertiesを作成
cp local.properties.example local.properties
# エディタでSDKパスを設定

# Debug APKビルド
./gradlew assembleDebug

# Release APKビルド  
./gradlew assembleRelease
```

### APKの場所

```
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk
└── release/
    └── app-release-unsigned.apk
```

## リリース手順

### 1. バージョン番号の更新

`app/build.gradle`:
```gradle
defaultConfig {
    versionCode 2
    versionName "1.1"
}
```

### 2. Gitタグの作成

```bash
git tag v1.1
git push origin v1.1
```

### 3. 自動リリース

タグをプッシュすると、GitHub Actionsが自動的に：
- APKをビルド
- GitHubリリースを作成
- APKを添付

## 署名付きAPKの作成

### キーストアの生成

```bash
keytool -genkey -v -keystore release-key.keystore -alias my-key-alias \
  -keyalg RSA -keysize 2048 -validity 10000
```

### app/build.gradleに追加

```gradle
android {
    signingConfigs {
        release {
            storeFile file('release-key.keystore')
            storePassword 'your-password'
            keyAlias 'my-key-alias'
            keyPassword 'your-password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
        }
    }
}
```

### GitHub Secretsに追加

1. GitHubリポジトリ → Settings → Secrets
2. 以下を追加:
   - `KEYSTORE_FILE` (Base64エンコードされたkeystore)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

## CI/CD設定のカスタマイズ

### ビルド条件の変更

`.github/workflows/android-build.yml`:
```yaml
on:
  push:
    branches: [ main, develop ]  # developブランチも追加
  pull_request:
    branches: [ main ]
```

### 自動テストの追加

```yaml
- name: Run tests
  run: ./gradlew test
  
- name: Run instrumented tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedCheck
```

### Slackへの通知

```yaml
- name: Notify Slack
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## トラブルシューティング

### gradlew: Permission denied

```bash
chmod +x gradlew
git add gradlew
git commit -m "Fix gradlew permissions"
git push
```

### ビルドタイムアウト

`.github/workflows/android-build.yml`:
```yaml
jobs:
  build:
    timeout-minutes: 30  # デフォルトは60分
```

### キャッシュのクリア

GitHubリポジトリ → Actions → Caches → すべて削除

---

**更新日**: 2025年10月16日

