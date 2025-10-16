# ネイティブライブラリ情報

## 📦 含まれているライブラリ

### OPPO/OnePlus - モーション予測ライブラリ

**ファイル名**: `libforecast.so`  
**バージョン**: 2.0.0  
**説明**: タッチポイントの軌跡を予測し、低遅延描画を実現

#### サポートABI
- ✅ arm64-v8a (64bit ARM)
- ❌ armeabi-v7a (32bit ARM) - 必要に応じて元APKから抽出

#### 機能
- モーション予測
- リフレッシュレート最適化
- DPI対応
- 筆圧・傾き検出

---

## 🔧 使用方法

### 1. プロジェクトへの配置

```
your-project/
└── app/
    └── src/
        └── main/
            └── jniLibs/
                └── arm64-v8a/
                    └── libforecast.so  # ← ここに配置
```

### 2. Javaでのロード

```java
static {
    System.loadLibrary("forecast");
}
```

### 3. ネイティブメソッドの使用

```java
public class NativeForecast {
    public static native long create(long handle);
    public static native void destroy(long handle);
    public static native TouchPointInfo predictTouchPoint(long handle);
    // ...
}
```

---

## ⚠️ 重要な注意事項

### ライセンス

このライブラリはHiPaintアプリ (com.aige.hipaint v5.0.10-188) から抽出されたものです。

**商用利用について**:
- この抽出されたライブラリの商用利用は推奨しません
- 商用アプリの場合は、OPPOから正式なSDKを入手してください
- OPPOの利用規約を必ず確認してください

### 取得方法

正式なSDKは以下から入手できる可能性があります：
- OPPO Developer Platform
- OnePlus Developer Portal
- 直接OPPOに問い合わせ

### 代替手段

正式なSDKが入手できない場合：
- 基本的なMotionEvent APIを使用
- 独自の予測アルゴリズムを実装
- SonarPenなどのサードパーティソリューションを使用

---

## 📊 技術仕様

### ライブラリサイズ
- arm64-v8a: 約 XXX KB

### 依存関係
なし（スタンドアロンライブラリ）

### 最小API要件
- Android 8.0 (API 26) 以降

### パフォーマンス
- 予測レイテンシー: < 1ms
- メモリ使用量: < 10MB
- CPU使用率: < 5%

---

## 🔍 トラブルシューティング

### ライブラリが読み込めない

**エラーメッセージ**:
```
java.lang.UnsatisfiedLinkError: dlopen failed: library "libforecast.so" not found
```

**原因と解決策**:

1. **ファイルが存在しない**
   - jniLibs/arm64-v8a/ に配置されているか確認
   - ファイル名が正確に `libforecast.so` か確認

2. **ABIが一致しない**
   - デバイスのABIを確認: `adb shell getprop ro.product.cpu.abi`
   - 対応するABI用のライブラリを配置

3. **Gradleの設定不足**
   ```gradle
   android {
       defaultConfig {
           ndk {
               abiFilters 'arm64-v8a'
           }
       }
   }
   ```

4. **ビルドキャッシュの問題**
   - Clean Project を実行
   - Rebuild Project を実行

### シンボルが見つからない

**エラーメッセージ**:
```
java.lang.UnsatisfiedLinkError: No implementation found for long NativeForecast.create()
```

**解決策**:
- JNIメソッドのシグネチャを確認
- パッケージ名とネイティブメソッド名が一致しているか確認

---

## 📚 参考資料

- [JNI仕様](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Android NDK ガイド](https://developer.android.com/ndk/guides)
- [実装ガイド](../docs/implementation-guide.md)

---

**ライブラリ情報 v1.0**  
**最終更新**: 2025年10月16日

