# アプリアイコンの準備

## 📱 アイコンについて

Android Studioでプロジェクトを開くと、デフォルトのアイコンが自動生成されます。

## 🎨 カスタムアイコンの作成

### 方法1: Android Studio Image Asset Studio（推奨）

1. Android Studioでプロジェクトを開く
2. `res` フォルダを右クリック
3. `New` → `Image Asset` を選択
4. Icon Typeを選択:
   - Launcher Icons (Adaptive and Legacy)
5. アイコン画像を選択または作成
6. `Next` → `Finish`

自動的に以下のサイズが生成されます：
- mipmap-mdpi: 48x48
- mipmap-hdpi: 72x72
- mipmap-xhdpi: 96x96
- mipmap-xxhdpi: 144x144
- mipmap-xxxhdpi: 192x192

### 方法2: 手動で配置

各サイズのic_launcher.pngを準備し、対応するディレクトリに配置：

```
res/
├── mipmap-mdpi/ic_launcher.png (48x48)
├── mipmap-hdpi/ic_launcher.png (72x72)
├── mipmap-xhdpi/ic_launcher.png (96x96)
├── mipmap-xxhdpi/ic_launcher.png (144x144)
└── mipmap-xxxhdpi/ic_launcher.png (192x192)
```

### 方法3: オンラインツール

以下のツールで自動生成：
- https://romannurik.github.io/AndroidAssetStudio/
- https://easyappicon.com/
- https://appicon.co/

1. 512x512のアイコン画像をアップロード
2. すべてのサイズをダウンロード
3. resフォルダに配置

## 🖼️ アイコンデザイン推奨

### スタイル
- シンプルなペン/鉛筆のアイコン
- ノートブックのアイコン
- 描画ツールのアイコン

### カラー
- Primary: #6200EE (紫)
- Accent: #03DAC5 (ターコイズ)

### サイズ
- 基本サイズ: 512x512 (高解像度)
- 背景: 透明またはソリッドカラー

## 📝 現在の状態

現在、アイコン用ディレクトリは作成済みですが、画像ファイルは未配置です。

### デフォルトアイコン

Android Studioでプロジェクトを開くと、デフォルトの緑色のロボットアイコンが自動的に使用されます。

### カスタマイズ

上記の方法でカスタムアイコンを作成・配置してください。

---

**更新日**: 2025年10月16日

