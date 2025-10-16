# 統合チェックリスト

プロジェクトへの統合を完了するためのチェックリスト

## ✅ Phase 1: ファイル配置

- [ ] **ネイティブライブラリ**
  - [ ] `libforecast.so` を `app/src/main/jniLibs/arm64-v8a/` にコピー
  - [ ] ファイルサイズを確認（破損チェック）
  - [ ] 権限を確認（読み取り可能）

- [ ] **Javaファイル - インターフェース**
  - [ ] `TouchPointInfo.java` をコピー
  - [ ] `NativeForecast.java` をコピー
  - [ ] `MotionPredictor.java` をコピー
  - [ ] パッケージ名: `com.oplusos.vfxsdk.forecast` のまま（重要！）

- [ ] **Javaファイル - ハンドラー（オプション：統合マネージャー使用の場合）**
  - [ ] `OppoStylusHandler.java` をコピー
  - [ ] `UnifiedStylusManager.java` をコピー
  - [ ] パッケージ名を自分のプロジェクトに合わせて変更

## ✅ Phase 2: プロジェクト設定

- [ ] **build.gradle (app)**
  ```gradle
  android {
      defaultConfig {
          minSdk 26  // 最小API 26
          ndk {
              abiFilters 'arm64-v8a'
          }
      }
  }
  
  dependencies {
      implementation 'androidx.appcompat:appcompat:1.6.1'
  }
  ```

- [ ] **AndroidManifest.xml**
  ```xml
  <uses-permission android:name="android.permission.RECORD_AUDIO" />
  <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
  ```

- [ ] **ProGuard設定 (リリースビルドの場合)**
  ```proguard
  -keep class com.oplusos.vfxsdk.forecast.** { *; }
  -keepclassmembers class com.oplusos.vfxsdk.forecast.** {
      native <methods>;
  }
  ```

## ✅ Phase 3: コード実装

### 最小実装（OPPO/OnePlusのみ）

- [ ] **MotionPredictorの初期化**
  ```java
  MotionPredictor predictor = new MotionPredictor();
  predictor.setRefreshRate(120.0f);
  predictor.setDpi(440.0f, 440.0f);
  predictor.setMaxPredictTime(16.0f);
  ```

- [ ] **タッチイベント処理**
  ```java
  TouchPointInfo point = new TouchPointInfo(x, y, pressure, tilt, timestamp);
  predictor.pushTouchPoint(point);
  TouchPointInfo predicted = predictor.predictTouchPoint();
  ```

- [ ] **リソース解放**
  ```java
  @Override
  protected void onDestroy() {
      predictor.destroy();
  }
  ```

### 完全実装（マルチベンダー対応）

- [ ] **UnifiedStylusManagerの使用**
  ```java
  UnifiedStylusManager manager = new UnifiedStylusManager(this, callback);
  manager.initialize(this);
  ```

- [ ] **DrawViewへの統合**
  ```java
  drawView.setStylusManager(manager);
  ```

## ✅ Phase 4: テストとデバッグ

- [ ] **ビルド確認**
  - [ ] Sync Gradle成功
  - [ ] Clean Build成功
  - [ ] APKビルド成功

- [ ] **実行時確認**
  - [ ] アプリ起動成功
  - [ ] クラッシュなし
  - [ ] ライブラリロード成功（Logcatで確認）

- [ ] **機能テスト**
  - [ ] タッチ入力が動作
  - [ ] 予測ポイント取得成功
  - [ ] 筆圧が反映（対応デバイス）
  - [ ] スムーズな描画

## ✅ Phase 5: 最適化

- [ ] **パフォーマンス**
  - [ ] リフレッシュレートの自動検出
  - [ ] DPIの自動取得
  - [ ] メモリリーク チェック

- [ ] **エラーハンドリング**
  - [ ] try-catchの追加
  - [ ] nullチェック
  - [ ] フォールバック処理

- [ ] **ログ**
  - [ ] デバッグログの追加
  - [ ] リリースビルドでログ無効化

## 🔧 トラブルシューティングチェック

問題が発生した場合、以下を確認：

### ライブラリロードエラー

- [ ] ファイルパスが正しい
- [ ] ABIが一致している
- [ ] System.loadLibrary()が呼ばれている
- [ ] パッケージ名が正しい

### クラッシュ

- [ ] Logcatでスタックトレース確認
- [ ] メモリ不足ではない
- [ ] UIスレッドで重い処理していない

### 予測が機能しない

- [ ] reset()が呼ばれている
- [ ] pushTouchPoint()が呼ばれている
- [ ] リフレッシュレートが設定されている
- [ ] デバイスがOPPO/OnePlusである

## 📊 検証項目

### 必須テスト

- [ ] OPPO/OnePlusデバイスでテスト
- [ ] 他のデバイスでフォールバック動作確認
- [ ] 回転時の動作確認
- [ ] バックグラウンド復帰時の動作確認

### オプションテスト

- [ ] 異なる画面サイズでテスト
- [ ] 異なるリフレッシュレートでテスト
- [ ] 長時間使用テスト
- [ ] メモリプロファイリング

## ✨ 完了確認

すべてのチェックボックスが完了したら：

- [ ] ドキュメントを確認
- [ ] コードレビュー
- [ ] ユーザーテスト
- [ ] リリース準備完了

---

**統合チェックリスト v1.0**  
**最終更新**: 2025年10月16日

## 📞 ヘルプ

詳細は以下を参照：
- [SETUP.md](SETUP.md) - 詳細セットアップガイド
- [README.md](README.md) - パッケージ概要
- [実装ガイド](../docs/implementation-guide.md) - 完全なコード例

