# スタイラスAPI実装パッケージ

このパッケージには、すぐに使える完全な実装が含まれています。

## 📦 パッケージ内容

```
implementation-package/
├── README.md                    # このファイル
├── SETUP.md                     # セットアップガイド
├── libs/                        # ネイティブライブラリ
│   ├── oppo-forecast/          # OPPO/OnePlus用
│   │   └── arm64-v8a/
│   │       └── libforecast.so  # モーション予測ライブラリ
│   └── README-LIBS.md          # ライブラリ情報
├── java/                        # Javaソースコード
│   ├── stylus/                 # スタイラスハンドラー
│   │   ├── OppoStylusHandler.java
│   │   ├── SamsungSPenHandler.java
│   │   ├── HuaweiHonorStylusHandler.java
│   │   ├── SonarPenHandler.java
│   │   └── UnifiedStylusManager.java
│   ├── view/                   # カスタムビュー
│   │   └── UnifiedDrawView.java
│   └── interfaces/             # インターフェース定義
│       ├── TouchPointInfo.java
│       └── StylusEventCallback.java
├── res/                        # リソースファイル
│   ├── layout/
│   │   └── activity_main.xml
│   ├── menu/
│   │   └── menu_main.xml
│   └── values/
│       ├── colors.xml
│       └── strings.xml
└── AndroidManifest-template.xml # マニフェストテンプレート
```

## 🚀 クイックスタート

### 1. ライブラリをコピー

```bash
# OPPO/OnePlusネイティブライブラリ
cp libs/oppo-forecast/arm64-v8a/libforecast.so \
   your-project/app/src/main/jniLibs/arm64-v8a/
```

### 2. Javaファイルをコピー

```bash
cp -r java/* your-project/app/src/main/java/com/yourapp/
```

### 3. 依存関係を追加

`build.gradle` に以下を追加：

```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
}
```

### 4. 権限を追加

`AndroidManifest.xml` に以下を追加：

```xml
<!-- SonarPen用 -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Samsung S Pen用 -->
<uses-permission android:name="com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE" />
```

### 5. 使用する

```java
UnifiedStylusManager manager = new UnifiedStylusManager(this, callback);
manager.initialize(this);
drawView.setStylusManager(manager);
```

## 📖 詳細ドキュメント

- [セットアップガイド](SETUP.md) - 詳細なインストール手順
- [実装ガイド](../docs/implementation-guide.md) - 完全なコード例
- [API仕様書](../docs/stylus-api-documentation.md) - 技術詳細

## ⚠️ 重要な注意事項

### ライセンスについて

各ベンダーのSDKには個別のライセンスが適用されます：

- **OPPO/OnePlus SDK**: OPPOの利用規約に従う
- **Samsung S Pen SDK**: Samsungの利用規約に従う
- **Huawei/Honor SDK**: Huaweiの利用規約に従う
- **SonarPen SDK**: SonarPenのライセンスに従う

商用利用の場合は、各ベンダーのライセンスを確認してください。

### ネイティブライブラリについて

- `libforecast.so`: OPPO/OnePlusのモーション予測ライブラリ
- このライブラリはHiPaintアプリから抽出されたものです
- 実際の使用には、OPPOから正式なSDKを入手することを推奨します

## 🔧 トラブルシューティング

### ライブラリが読み込めない

```
UnsatisfiedLinkError: dlopen failed: library "libforecast.so" not found
```

**解決策**:
1. `jniLibs` フォルダに正しく配置されているか確認
2. ABIが正しいか確認（arm64-v8a、armeabi-v7a など）
3. `build.gradle` で正しいABIが含まれているか確認

### 権限エラー

```
SecurityException: Permission denied
```

**解決策**:
1. AndroidManifest.xml に権限を追加
2. 実行時権限リクエストを実装（Android 6.0+）

## 📞 サポート

問題が発生した場合：
1. [SETUP.md](SETUP.md) を確認
2. [トラブルシューティングガイド](../docs/implementation-guide.md#トラブルシューティング) を参照
3. サンプルコードと比較

---

**パッケージバージョン**: 1.0  
**作成日**: 2025年10月16日

