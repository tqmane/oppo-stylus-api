# Stylus API Documentation Project

このプロジェクトは、HiPaint (com.aige.hipaint) APKファイルから抽出したスタイラスAPIの包括的な解析ドキュメントを提供します。

## 📚 ドキュメント

### API仕様書
- **[日本語版](./stylus-api-documentation.md)** - 詳細なAPI仕様ドキュメント
- **[English Version](./stylus-api-documentation-en.md)** - Comprehensive API documentation

### 実装ガイド（コピー&ペーストで使える実装コード）
- **[実装ガイド（日本語）](./implementation-guide.md)** - 即座に使える完全な実装例
- **[Implementation Guide (English)](./implementation-guide-en.md)** - Production-ready code examples
- **[クイックリファレンス](./quick-reference.md)** - すぐに参照できる簡潔なコード集 ⚡

## 🎯 サポートされているスタイラスベンダー

このドキュメントでは、以下のスタイラスAPIについて詳細に解説しています：

### 1. OPPO/OnePlus (VFX SDK) ⭐
- **パッケージ**: `com.oplusos.vfxsdk.forecast`
- **主要機能**: モーション予測、高リフレッシュレート対応
- **バージョン**: 2.0.0
- **特徴**: 最先端のモーション予測アルゴリズム、ネイティブC++実装

### 2. Samsung S Pen Remote
- **パッケージ**: `com.samsung.android.sdk.penremote`
- **主要機能**: ボタンイベント、エアモーション
- **バージョン**: 1.0.1
- **特徴**: Galaxy専用、リモート操作機能

### 3. Huawei Stylus Pen Engine
- **パッケージ**: `com.huawei.stylus.penengine`
- **主要機能**: ストローク予測
- **特徴**: M-Pencilサポート、予測エンジン

### 4. Honor Pen Engine (MagicX)
- **パッケージ**: `com.hihonor.android.magicx.app.penengine`
- **主要機能**: ストローク予測、カラーピッカー
- **特徴**: Huawei APIベース＋独自機能

### 5. SonarPen (サードパーティ)
- **パッケージ**: `com.greenbulb.sonarpen`
- **主要機能**: 音響ベース筆圧検出
- **バージョン**: 3.11 (2024042101)
- **特徴**: 汎用デバイス対応、ハードウェア不要

## 🔍 主な内容

各ベンダーのドキュメントには以下の情報が含まれています：

- **クラス構造**: 主要クラスとそのメソッド
- **使用例**: 実際のコード例
- **技術的詳細**: アルゴリズム、パラメータ、最適化
- **統合方法**: アプリへの組み込み方法
- **ベストプラクティス**: 推奨される使用方法

## 📊 機能比較

| ベンダー | モーション予測 | 筆圧検出 | 傾き検出 | ボタンイベント | 特殊機能 |
|---------|--------------|---------|---------|---------------|----------|
| OPPO/OnePlus | ✅ 高度 | ✅ | ✅ | ✅ | リフレッシュレート最適化 |
| Samsung | ❌ | ⚠️ S Pen | ⚠️ S Pen | ✅ | エアモーション |
| Huawei | ✅ | ✅ | ✅ | ✅ | - |
| Honor | ✅ | ✅ | ✅ | ✅ | カラーピッカー |
| SonarPen | ❌ | ✅ 音響 | ❌ | ✅ | 汎用デバイス対応 |

## 🚀 特にOPPO/OnePlusについて

ドキュメントでは特にOPPO/OnePlusのVFX SDKについて深く解析しています：

- **TouchPointInfo**: X, Y座標、筆圧、傾き角度、タイムスタンプ
- **MotionPredictor**: 高度な軌跡予測アルゴリズム
- **NativeForecast**: JNIインターフェース、ネイティブライブラリ
- **最適化機能**: DPI設定、リフレッシュレート調整、予測時間設定

## 📁 プロジェクト構造

```
Pen-API/
├── docs/
│   ├── README.md (このファイル)
│   ├── stylus-api-documentation.md (日本語版)
│   └── stylus-api-documentation-en.md (英語版)
├── extract/
│   └── src/
│       ├── sources/ (Javaソースコード)
│       └── resources/ (リソースファイル)
├── tools/
│   └── jadx/ (デコンパイルツール)
└── com.aige.hipaint_5.0.10-188.apk (元のAPKファイル)
```

## 🛠️ 解析に使用したツール

- **JADX**: APKデコンパイルツール (v1.5.1)
- APK解析とJavaソースコード抽出に使用

## 📝 ソースコードの場所

抽出されたソースコードは以下のディレクトリにあります：

```
extract/src/sources/
├── com/oplusos/vfxsdk/forecast/          # OPPO/OnePlus API
├── com/samsung/android/sdk/penremote/     # Samsung API
├── com/huawei/stylus/penengine/          # Huawei API
├── com/hihonor/android/magicx/app/penengine/ # Honor API
└── com/greenbulb/sonarpen/               # SonarPen API
```

## 🚀 実装ガイドの内容

**[実装ガイド](./implementation-guide.md)** には以下の完全な実装コードが含まれています：

### 1. ベンダー別ハンドラークラス
すぐに使える各ベンダー専用のハンドラークラス：
- `OppoStylusHandler` - OPPO/OnePlus用（モーション予測対応）
- `SamsungSPenHandler` - Samsung S Pen用（ボタン＆エアモーション対応）
- `HuaweiHonorStylusHandler` - Huawei/Honor用（ストローク予測対応）
- `SonarPenHandler` - SonarPen用（音響ベース筆圧検出）

### 2. 統合マネージャー
デバイスを自動検出し、適切なAPIを使用：
```java
UnifiedStylusManager stylusManager = new UnifiedStylusManager(context, callback);
stylusManager.initialize(activity);
// 自動的に最適なAPIが選択される
```

### 3. カスタムDrawView
筆圧対応の完全な描画ビュー実装：
```java
UnifiedDrawView drawView = findViewById(R.id.draw_view);
drawView.setStylusManager(stylusManager);
// 筆圧、予測ポイント、Undo機能など完備
```

### 4. 完全なサンプルアプリケーション
- MainActivity（イベント処理完備）
- レイアウトXML（UI設計済み）
- AndroidManifest.xml（必要な権限設定済み）
- build.gradle（依存関係設定例）

## 💡 クイックスタート

### 最小限のコード例

```java
// たった3ステップで使える！
// 1. マネージャーを初期化
UnifiedStylusManager manager = new UnifiedStylusManager(this, callback);
manager.initialize(this);

// 2. DrawViewに設定
drawView.setStylusManager(manager);

// 3. これだけ！自動的にデバイスに最適なAPIが使われます
```

## 📖 さらに詳しく

詳細なAPI仕様、メソッドの説明、技術的詳細については、以下のドキュメントをご覧ください：

- **[日本語版の完全ドキュメント](./stylus-api-documentation.md)**
- **[English Full Documentation](./stylus-api-documentation-en.md)**

## 🔗 関連情報

- **アプリ**: HiPaint - Drawing & Painting App
- **パッケージ名**: com.aige.hipaint
- **バージョン**: 5.0.10-188
- **解析日**: 2025年10月16日

## 📄 ライセンス

このドキュメントは解析目的で作成されました。各スタイラスAPIのライセンスについては、それぞれのベンダーの規約をご確認ください。

---

**ドキュメントバージョン**: 1.0  
**最終更新**: 2025年10月16日

