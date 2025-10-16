# スタイラスAPI解析ドキュメント

**アプリ**: HiPaint (com.aige.hipaint) v5.0.10-188  
**解析日**: 2025年10月16日

## 目次

1. [概要](#概要)
2. [OPPO/OnePlus スタイラスAPI](#oppooneplus-スタイラスapi)
3. [Samsung S Penリモート API](#samsung-s-penリモート-api)
4. [Huawei スタイラスペンエンジンAPI](#huawei-スタイラスペンエンジンapi)
5. [Honor ペンエンジンAPI](#honor-ペンエンジンapi)
6. [SonarPen API](#sonarpen-api)
7. [まとめ](#まとめ)

---

## 概要

HiPaintアプリは、複数のスタイラスベンダーのAPIを統合し、様々なデバイスでペン入力機能をサポートしています。本ドキュメントでは、特にOPPO/OnePlus向けのAPIを中心に、各ベンダーのスタイラスAPIの詳細を解析します。

**サポートされているスタイラスベンダー**:
- OPPO/OnePlus (OPlusOS VFX SDK)
- Samsung (S Pen Remote SDK)
- Huawei (Stylus Pen Engine)
- Honor (MagicX Pen Engine)
- SonarPen (サードパーティ音響ベース)

---

## OPPO/OnePlus スタイラスAPI

### パッケージ
`com.oplusos.vfxsdk.forecast`

### 概要

OPPO/OnePlusのスタイラスAPIは**モーション予測（Motion Prediction）**機能を提供し、ペン入力の遅延を最小限に抑え、より自然な描画体験を実現します。このSDKは、タッチポイントの軌跡を予測することで、リフレッシュレートとの同期を改善します。

### 主要クラス

#### 1. MotionPredictor

**ファイル**: `com/oplusos/vfxsdk/forecast/MotionPredictor.java`

**説明**: タッチイベントのモーション予測を管理するメインクラス。

**定数**:
- `VERSION`: "VERSION_2.0.0"
- `TAG`: "Forecast:MotionPredictor"

**主要メソッド**:

```java
// コンストラクタ
public MotionPredictor()

// ネイティブハンドルを作成
void create()

// リフレッシュレートを設定（Hz）
public void setRefreshRate(float refreshRate)

// 画面のDPIを設定
public void setDpi(float dpiX, float dpiY)

// 最大予測時間を設定（ミリ秒）
public void setMaxPredictTime(float maxPredictTime)

// 予測システムをリセット
public void reset()

// タッチポイントをキューに追加
public void pushTouchPoint(TouchPointInfo touchPointInfo)

// 次のタッチポイントを予測
public TouchPointInfo predictTouchPoint()

// リソースを解放
public void destroy()
```

**使用例**:
```java
MotionPredictor predictor = new MotionPredictor();
predictor.setRefreshRate(120.0f);  // 120Hzディスプレイ
predictor.setDpi(440.0f, 440.0f);
predictor.setMaxPredictTime(16.0f); // 16ms予測

// タッチイベント受信時
TouchPointInfo currentPoint = new TouchPointInfo(x, y, pressure, tilt, timestamp);
predictor.pushTouchPoint(currentPoint);

// 予測ポイント取得
TouchPointInfo predictedPoint = predictor.predictTouchPoint();
```

#### 2. TouchPointInfo

**ファイル**: `com/oplusos/vfxsdk/forecast/TouchPointInfo.java`

**説明**: タッチポイントのデータを保持するクラス。

**フィールド**:
```java
public float x;           // X座標
public float y;           // Y座標
public float pressure;    // 筆圧 (0.0 ~ 1.0)
public float axisTilt;    // ペンの傾き角度
public long timestamp;    // タイムスタンプ（ミリ秒）
```

**コンストラクタ**:
```java
// パラメータ付きコンストラクタ
public TouchPointInfo(float x, float y, float pressure, float axisTilt, long timestamp)

// デフォルトコンストラクタ（すべて0で初期化）
public TouchPointInfo()
```

#### 3. NativeForecast

**ファイル**: `com/oplusos/vfxsdk/forecast/NativeForecast.java`

**説明**: ネイティブライブラリへのJNIインターフェース。

**ネイティブメソッド**:
```java
// ネイティブハンドルを作成
public static native long create(long handle);

// 予測エンジンを破棄
public static native void destroy(long handle);

// タッチポイントをプッシュ
public static native void pushTouchPoint(long handle, TouchPointInfo info);

// 予測タッチポイントを取得
public static native TouchPointInfo predictTouchPoint(long handle);

// システムをリセット
public static native void reset(long handle);

// リフレッシュレートを設定
public static native void setRefreshRate(long handle, float rate);

// DPIを設定
public static native void setDpi(long handle, float dpiX, float dpiY);

// 最大予測時間を設定
public static native void setMaxPredictTime(long handle, float maxTime);
```

**ロードされるライブラリ**:
```java
static {
    System.loadLibrary("forecast");
}
```

### 特徴と利点

1. **低遅延描画**: モーション予測により、ペン入力の遅延を最小化
2. **高リフレッシュレート対応**: 最大120Hz以上のディスプレイに対応
3. **筆圧・傾き対応**: 筆圧と傾き角度情報を含む詳細なタッチデータ
4. **カスタマイズ可能**: DPI、リフレッシュレート、予測時間を調整可能

### 技術的詳細

- **予測アルゴリズム**: ネイティブC++実装による高速な軌跡予測
- **タイムスタンプ管理**: 相対時間を使用し、システムクロックとの同期
- **メモリ管理**: ネイティブハンドルを使用した効率的なリソース管理

### OPPO/OnePlusデバイスでの統合

HiPaintアプリでは、OPPO/OnePlusデバイスを検出すると自動的にこのAPIを有効化します。DrawMainUIクラスには以下のアクション定義があります：

```java
public static final String ACTION_OPPO_STYLUS_BUTTON_DOUBLE_PRESSED = 
    "com.oplus.ipemanager.action.PENCIL_DOUBLE_CLICK";
```

これにより、OPPOスタイラスのダブルクリックボタンイベントもサポートされています。

---

## Samsung S Penリモート API

### パッケージ
`com.samsung.android.sdk.penremote`

### 概要

Samsung S Pen Remote SDKは、Galaxy NoteシリーズのS Penのリモート機能（ボタンイベント、エアモーション）を提供します。

### 主要クラス

#### 1. SpenRemote

**ファイル**: `com/samsung/android/sdk/penremote/SpenRemote.java`

**説明**: S Penリモートサービスへの接続を管理するメインクラス。

**定数**:
- `SDK_VERSION`: 16777217 (バージョン 1.0.1)
- `FEATURE_BUTTON`: ボタン機能
- `FEATURE_AIR_MOTION`: エアモーション機能
- `BIND_PACKAGE_NAME`: "com.samsung.android.service.aircommand"

**主要メソッド**:
```java
// シングルトンインスタンス取得
public static SpenRemote getInstance()

// S Penサービスに接続
public void connect(Context context, ConnectionResultCallback callback)

// 接続を切断
public void disconnect(Context context)

// 機能が有効かチェック
public boolean isFeatureEnabled(int featureType)

// 接続状態リスナーを設定
public void setConnectionStateChangeListener(ConnectionStateChangeListener listener)

// 接続状態を確認
public boolean isConnected()

// バージョン情報を取得
public String getVersionName()
public int getVersionCode()
```

**インターフェース**:
```java
// 接続結果コールバック
public interface ConnectionResultCallback {
    void onSuccess(SpenUnitManager spenUnitManager);
    void onFailure(int errorCode);
}

// 接続状態変更リスナー
public interface ConnectionStateChangeListener {
    void onChange(int state);
}
```

**エラーコード**:
- `CONNECTION_FAILED` (-2): 接続失敗
- `UNSUPPORTED_DEVICE` (-1): 非対応デバイス
- `UNKNOWN` (-100): 不明なエラー

**接続状態**:
- `CONNECTED` (0): 接続済み
- `DISCONNECTED` (-1): 切断済み
- `DISCONNECTED_BY_UNKNOWN_REASON` (-2): 不明な理由で切断

#### 2. ButtonEvent

**ファイル**: `com/samsung/android/sdk/penremote/ButtonEvent.java`

**説明**: S Penボタンイベントを表すクラス。

**定数**:
```java
public static final int ACTION_DOWN = 0;
public static final int ACTION_UP = 1;
```

**メソッド**:
```java
public int getAction()        // ボタンアクションを取得
public long getTimeStamp()    // タイムスタンプを取得
```

#### 3. AirMotionEvent

**ファイル**: `com/samsung/android/sdk/penremote/AirMotionEvent.java`

**説明**: S Penのエアモーション（空中でのジェスチャー）イベント。

**フィールド**:
```java
public static final int DELTA_X = 0;
public static final int DELTA_Y = 1;
```

**メソッド**:
```java
public float getDeltaX()      // X軸の移動量
public float getDeltaY()      // Y軸の移動量
public long getTimeStamp()    // タイムスタンプ
```

#### 4. SpenEvent

**ファイル**: `com/samsung/android/sdk/penremote/SpenEvent.java`

**説明**: 汎用S Penイベント基底クラス。

**イベントタイプ**:
```java
public static final int TYPE_BUTTON = 0;
public static final int TYPE_AIR_MOTION = 1;
```

**メソッド**:
```java
public float getValue(int index)
public void setValue(int index, float value)
public long getTimeStamp()
public void setTimeStamp(long timestamp)
```

### 使用例

```java
// S Penサービスに接続
SpenRemote spenRemote = SpenRemote.getInstance();
spenRemote.connect(this, new SpenRemote.ConnectionResultCallback() {
    @Override
    public void onSuccess(SpenUnitManager manager) {
        // 接続成功
        manager.registerSpenEventListener(new SpenEventListener() {
            @Override
            public void onButtonEvent(ButtonEvent event) {
                if (event.getAction() == ButtonEvent.ACTION_DOWN) {
                    // ボタン押下処理
                }
            }
            
            @Override
            public void onAirMotionEvent(AirMotionEvent event) {
                float deltaX = event.getDeltaX();
                float deltaY = event.getDeltaY();
                // エアモーション処理
            }
        });
    }
    
    @Override
    public void onFailure(int errorCode) {
        // エラー処理
    }
});
```

### 必要な権限

```xml
<uses-permission android:name="com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE"/>
```

---

## Huawei スタイラスペンエンジンAPI

### パッケージ
`com.huawei.stylus.penengine`

### 概要

Huaweiのスタイラスペンエンジンは、HuaweiデバイスのM-Pencilなどのスタイラスをサポートし、ストローク予測（Stroke Estimate）機能を提供します。

### 主要クラス

#### 1. HwPenEngineManager

**ファイル**: `com/huawei/stylus/penengine/HwPenEngineManager.java`

**説明**: ペンエンジンの可用性をチェックするマネージャークラス。

**メソッド**:
```java
// ペンエンジンランタイムが利用可能かチェック
public static boolean isEngineRuntimeAvailable(Context context)

// 特定の機能がサポートされているかチェック
public static boolean isSupportFeature(Context context, int featureLevel)

// APIレベルを取得
public static int getApiLevel()

// APIレベルの互換性をチェック
public static boolean isApiLevelCompatible(int requiredLevel)
```

**使用例**:
```java
if (HwPenEngineManager.isEngineRuntimeAvailable(context)) {
    // Huaweiペンエンジンが利用可能
    if (HwPenEngineManager.isSupportFeature(context, 1)) {
        // 機能レベル1がサポートされている
    }
}
```

#### 2. HwStrokeEstimate

**ファイル**: `com/huawei/stylus/penengine/estimate/HwStrokeEstimate.java`

**説明**: ストローク予測機能を提供するクラス。

**メソッド**:
```java
// ストローク予測機能が有効かチェック
public static synchronized boolean isFeatureEnable()

// 予測されたモーションイベントを取得
public static synchronized int getEstimateEvent(
    IHwRecycleQueue queue, 
    List<HwMotionEventInfo> eventList
)

// リフレッシュレートを設定
public static synchronized void setRefreshRate(float refreshRate)
```

#### 3. HwMotionEventInfo

**ファイル**: `com/huawei/stylus/penengine/estimate/HwMotionEventInfo.java`

**説明**: モーションイベント情報を保持するクラス。

**フィールドとメソッド**:
```java
// コンストラクタ
public HwMotionEventInfo(float x, float y, float press, float tilt, long eventTime)

// ゲッター
public float getX()
public float getY()
public float getPress()      // 筆圧
public float getTilt()       // 傾き
public long getEventTime()

// セッター
public void setX(float x)
public void setY(float y)
public void setPress(float press)
public void setTilt(float tilt)
public void setEventTime(long time)
```

### 使用例

```java
// ストローク予測の初期化
if (HwStrokeEstimate.isFeatureEnable()) {
    HwStrokeEstimate.setRefreshRate(90.0f);  // 90Hzディスプレイ
    
    // イベントキューの準備
    HwMotionEventQueue queue = new HwMotionEventQueue();
    List<HwMotionEventInfo> estimatedEvents = new ArrayList<>();
    
    // 予測イベントを取得
    int result = HwStrokeEstimate.getEstimateEvent(queue, estimatedEvents);
    
    if (result >= 0) {
        for (HwMotionEventInfo event : estimatedEvents) {
            float x = event.getX();
            float y = event.getY();
            float pressure = event.getPress();
            // 予測されたイベントを処理
        }
    }
}
```

### ボタンイベント

HiPaintアプリでは以下のアクションが定義されています：

```java
public static final String ACTION_HUAWEI_STYLUS_BUTTON_DOUBLE_PRESSED = 
    "com.huawei.stylus.action.BUTTON_DOUBLE_PRESSED";
```

---

## Honor ペンエンジンAPI

### パッケージ
`com.hihonor.android.magicx.app.penengine`

### 概要

HonorのペンエンジンAPI（MagicXペンエンジン）は、HuaweiのAPIと非常に類似していますが、Honorブランドのデバイス向けに最適化されています。また、カラーピッカー機能などの追加機能も提供します。

### 主要クラス

#### 1. HnPenEngineManager

**ファイル**: `com/hihonor/android/magicx/app/penengine/HnPenEngineManager.java`

**説明**: Honorペンエンジンの管理クラス。

**メソッド**:
```java
// ペンエンジンランタイムが利用可能かチェック
public static boolean isEngineRuntimeAvailable(Context context)

// ダブルクリック機能がサポートされているかチェック
public static boolean isSupportDoubleClick(Context context)

// カラーピッカー機能が利用可能かチェック
public static boolean checkColorPickerAvailable(Context context)

// カラーピッカーを表示
public static void showColorPicker(
    IHonorColorListener listener, 
    int initialColor, 
    Context context
)
```

**特徴**:
- Huawei APIと同様のストローク予測機能
- **カラーピッカー機能**: スタイラスでスクリーン上の色を直接抽出
- **ダブルクリック検出**: スタイラスボタンのダブルクリックをネイティブでサポート

#### 2. HnStrokeEstimate

**ファイル**: `com/hihonor/android/magicx/app/penengine/estimate/HnStrokeEstimate.java`

**説明**: Huaweiと同様のストローク予測機能。

**メソッド**:
```java
public static synchronized boolean isFeatureEnable()
public static synchronized int getEstimateEvent(
    IHnRecycleQueue queue, 
    List<HnMotionEventInfo> eventList
)
public static synchronized void setRefreshRate(float refreshRate)
```

#### 3. HnMotionEventInfo

**ファイル**: `com/hihonor/android/magicx/app/penengine/estimate/HnMotionEventInfo.java`

**説明**: HwMotionEventInfoと同じ構造。

**フィールド**:
```java
public float mAxisX;
public float mAxisY;
public float mPress;
public float mTilt;
public long mEventTime;
```

### カラーピッカーの使用例

```java
if (HnPenEngineManager.checkColorPickerAvailable(context)) {
    HnPenEngineManager.showColorPicker(
        new IHonorColorListener() {
            @Override
            public void onColorSelected(int color) {
                // 選択された色を処理
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
            }
        },
        Color.WHITE,  // 初期色
        context
    );
}
```

### ボタンイベント

```java
public static final String ACTION_HONOR_STYLUS_BUTTON_DOUBLE_PRESSED = 
    "com.hihonor.stylus.action.STYLUS_DOUBLE_CLICK";
```

---

## SonarPen API

### パッケージ
`com.greenbulb.sonarpen`

### 概要

SonarPenは、音響信号を使用して筆圧検出を実現するユニークなサードパーティ製スタイラスソリューションです。専用のハードウェアを必要とせず、標準的なスタイラスやタッチペンに筆圧機能を追加できます。

### 技術原理

- **音響ベース**: イヤホンジャックまたはUSB-Cから超音波信号を送信
- **圧力検出**: スタイラスが画面を押す強さに応じて音響信号の振幅が変化
- **リアルタイム処理**: AudioRecordを使用してマイクから音響信号をキャプチャ

### 主要クラス

#### SonarPenUtilities

**ファイル**: `com/greenbulb/sonarpen/SonarPenUtilities.java`

**説明**: SonarPenのメイン制御クラス（2000行以上の大規模クラス）。

**バージョン**: "3.11 (2024042101)"

**主要メソッド**:

```java
// コンストラクタ
public SonarPenUtilities(Context context)

// SonarPenを起動
public int start()

// SonarPenを停止
public void stop()

// ビューにSonarPen機能を追加
public void addSonarPenToView(View view)

// コールバックを設定
public void addSonarPenCallback(SonarPenCallBack callback)

// タッチイベントを変換
public MotionEvent translateTouchEvent(MotionEvent event)

// 現在の筆圧を取得
public double getCurrentPressure()

// 筆圧調整式を設定
public void setPressureTuneFormula(int formula)

// 手動キャリブレーション値を保存
public void saveManualReadings(float minValue, float maxValue)

// キャリブレーションアプリを起動
public void startCalibrateScreen()

// オーディオを一時停止
public boolean audioPause()

// オーディオを再開
public boolean audioResume()

// タッチサイズを使用するか設定
public void setUseTouchSize(boolean use)

// 筆圧スムージングオプションを変更
public boolean ChangePressureSmoothOption(int option)

// SonarPenが画面上にあるかチェック
public boolean isSonarPenOnScreen()

// USB-C接続かチェック
public boolean isSonarPenOnUSBC()

// 手動キャリブレーションを使用しているかチェック
public boolean isUsingManualCalibrate()

// ボタンが押されているかチェック
public boolean getSonicPenButtonPressed()
```

**キャリブレーション機能**:
```java
// 現在の読み取り値を取得
public void getCurrentReadingValue(SonarPenReadings readings)
public JSONObject getCurrReading()

// 手動設定値を保存
public void saveManualReadings(float minValue, float maxValue)

// 音量を手動設定
public void setManualCalVol(int volume)
```

**デバッグ機能**:
```java
public void setDebugLogOnOff(boolean enabled)
public boolean getDebugLogStatus()
```

### SonarPenCallBack インターフェース

```java
public interface SonarPenCallBack {
    // SonarPenのステータスが変化したとき
    void onSonarPenStatusChange(int status);
    
    // SonarPenボタンが押されたとき
    void onSonarPenButtonPressed();
}
```

### SonarPenReadings クラス

筆圧読み取り値を格納するデータクラス：

```java
public class SonarPenReadings {
    public float minValue;           // 最小振幅値
    public float maxValue;           // 最大振幅値
    public float currentValue;       // 現在の振幅値
    public float touchMinValue;      // タッチ時の最小値
    public float pressure;           // 計算された筆圧 (0.0 ~ 1.0)
    public float manualMin;          // 手動キャリブレーションの最小値
    public float manualMax;          // 手動キャリブレーションの最大値
    public int currentSoundVol;      // 現在の音量
    public float currentFeq;         // 現在の周波数
    public int audioReadStatus;      // オーディオ読み取り状態
    public boolean bLowFreq;         // 低周波モードかどうか
    public String extraInfo;         // 追加情報
}
```

### 使用例

```java
// SonarPen初期化
SonarPenUtilities sonarPen = new SonarPenUtilities(context);

// コールバック設定
sonarPen.addSonarPenCallback(new SonarPenCallBack() {
    @Override
    public void onSonarPenStatusChange(int status) {
        // ステータス変化処理
    }
    
    @Override
    public void onSonarPenButtonPressed() {
        // ボタン押下処理
    }
});

// ビューに追加
sonarPen.addSonarPenToView(drawView);

// 起動
int result = sonarPen.start();
if (result == 0) {
    // 正常に起動
}

// タッチイベント処理
@Override
public boolean onTouchEvent(MotionEvent event) {
    MotionEvent translatedEvent = sonarPen.translateTouchEvent(event);
    // 変換されたイベントを処理（筆圧情報が追加されている）
    return super.onTouchEvent(translatedEvent);
}

// 終了時
@Override
protected void onDestroy() {
    sonarPen.stop();
    super.onDestroy();
}
```

### 技術的詳細

**オーディオ設定**:
- サンプルレート: 44100 Hz
- チャンネル: モノラル (12)
- エンコーディング: PCM 16bit (2)
- デフォルト周波数: 9000 Hz
- 低周波モード: 250 Hz

**筆圧計算式**:
SonarPenは複数の筆圧調整式をサポート：
```java
// 式0（デフォルト）
pressure = 2.21622 * x^3 - 1.91606 * x^2 + 0.7018 * x + 0.00008

// 式1
pressure = 0.7752 * x^3 - 0.30993 * x^2 + 0.53326 * x - 0.00056
```

**キャリブレーション**:
- 自動キャリブレーション: 使用中に自動的に調整
- 手動キャリブレーション: ユーザーが最小/最大値を設定
- ファイルベース: `/SonarPen/manual.setting`に設定を保存

**権限要件**:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
```

---

## まとめ

### API機能比較表

| ベンダー | モーション予測 | 筆圧検出 | 傾き検出 | ボタンイベント | 特殊機能 |
|---------|--------------|---------|---------|---------------|----------|
| **OPPO/OnePlus** | ✅ 高度 | ✅ | ✅ | ✅ | リフレッシュレート最適化 |
| **Samsung** | ❌ | ⚠️ S Pen | ⚠️ S Pen | ✅ | エアモーション |
| **Huawei** | ✅ | ✅ | ✅ | ✅ | - |
| **Honor** | ✅ | ✅ | ✅ | ✅ | カラーピッカー |
| **SonarPen** | ❌ | ✅ 音響ベース | ❌ | ✅ | 汎用デバイス対応 |

### OPPO/OnePlus APIの優位性

1. **最先端のモーション予測**: バージョン2.0.0の成熟したアルゴリズム
2. **詳細なカスタマイズ**: DPI、リフレッシュレート、予測時間の細かい調整が可能
3. **高性能**: ネイティブC++実装による高速処理
4. **完全な筆圧・傾きサポート**: プロフェッショナルな描画アプリケーションに最適
5. **ハイエンドディスプレイ対応**: 120Hz+の高リフレッシュレートに最適化

### 推奨される使用シナリオ

**OPPO/OnePlus API**:
- プロフェッショナルな描画・イラストアプリ
- 低遅延が重要なアプリケーション
- 高リフレッシュレートディスプレイを活用するアプリ

**Samsung S Pen API**:
- Galaxyデバイス専用アプリ
- リモート操作が必要なアプリ（プレゼンテーションなど）
- エアジェスチャーを活用するアプリ

**Huawei/Honor API**:
- Huawei/Honorデバイスでの最適化が必要なアプリ
- カラーピッキング機能を活用するアプリ（Honorのみ）

**SonarPen API**:
- 幅広いデバイスで筆圧検出を提供したいアプリ
- 専用ハードウェアなしで筆圧を実現したい場合
- 予算制約のあるユーザー向けアプリ

### 統合のベストプラクティス

```java
public class StylusManager {
    private MotionPredictor oppoPredictor;
    private SpenRemote samsungSpen;
    private SonarPenUtilities sonarPen;
    
    public void initialize(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        
        if (manufacturer.contains("oppo") || manufacturer.contains("oneplus")) {
            // OPPO/OnePlus API初期化
            oppoPredictor = new MotionPredictor();
            oppoPredictor.setRefreshRate(getDisplayRefreshRate());
            oppoPredictor.setDpi(getDpiX(), getDpiY());
            
        } else if (manufacturer.contains("samsung")) {
            // Samsung API初期化
            samsungSpen = SpenRemote.getInstance();
            samsungSpen.connect(context, connectionCallback);
            
        } else if (manufacturer.contains("huawei")) {
            // Huawei API初期化
            if (HwPenEngineManager.isEngineRuntimeAvailable(context)) {
                HwStrokeEstimate.setRefreshRate(90.0f);
            }
            
        } else if (manufacturer.contains("honor")) {
            // Honor API初期化
            if (HnPenEngineManager.isEngineRuntimeAvailable(context)) {
                HnStrokeEstimate.setRefreshRate(90.0f);
            }
            
        } else {
            // フォールバック: SonarPen
            sonarPen = new SonarPenUtilities(context);
            sonarPen.start();
        }
    }
}
```

### 今後の開発における考慮事項

1. **APIバージョン管理**: 各ベンダーAPIのバージョンアップに対応
2. **パフォーマンス最適化**: デバイスごとの最適なパラメータ設定
3. **フォールバック戦略**: APIが利用できない場合の代替手段
4. **ユーザー設定**: キャリブレーションや感度調整のUI提供
5. **テスト**: 各デバイスでの徹底的な実機テスト

---

## 参考情報

### ファイルパス

- **OPPO/OnePlus**: `extract/src/sources/com/oplusos/vfxsdk/forecast/`
- **Samsung**: `extract/src/sources/com/samsung/android/sdk/penremote/`
- **Huawei**: `extract/src/sources/com/huawei/stylus/penengine/`
- **Honor**: `extract/src/sources/com/hihonor/android/magicx/app/penengine/`
- **SonarPen**: `extract/src/sources/com/greenbulb/sonarpen/`

### 関連クラス

- **メイン統合**: `com/aige/hipaint/draw/ui/DrawMainUI.java` (26819行)
- **ネイティブサーフェス**: `com/aige/hipaint/draw/opengl/NativeSurfaceView.java`
- **ペンレイヤー**: `com/aige/hipaint/draw/widget/PenLayerView.java`

---

**ドキュメントバージョン**: 1.0  
**最終更新**: 2025年10月16日

