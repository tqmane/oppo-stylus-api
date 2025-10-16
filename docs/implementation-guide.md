# スタイラスAPI実装ガイド - 実践的コード集

**対象**: 他のアプリに統合可能な実装コード  
**作成日**: 2025年10月16日

## 目次

1. [OPPO/OnePlus実装](#oppooneplus実装)
2. [Samsung S Pen実装](#samsung-s-pen実装)
3. [Huawei/Honor実装](#huaweihonor実装)
4. [SonarPen実装](#sonarpen実装)
5. [マルチベンダー統合マネージャー](#マルチベンダー統合マネージャー)
6. [カスタムDrawViewの実装](#カスタムdrawviewの実装)
7. [完全なサンプルアプリケーション](#完全なサンプルアプリケーション)

---

## OPPO/OnePlus実装

### 1. 基本的な実装クラス

```java
package com.example.drawapp.stylus;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.oplusos.vfxsdk.forecast.MotionPredictor;
import com.oplusos.vfxsdk.forecast.TouchPointInfo;

public class OppoStylusHandler {
    private static final String TAG = "OppoStylusHandler";
    private MotionPredictor motionPredictor;
    private Context context;
    private boolean isInitialized = false;
    
    // 設定値
    private float refreshRate = 120.0f;  // デフォルト120Hz
    private float maxPredictTime = 16.0f; // 16ms予測
    
    public OppoStylusHandler(Context context) {
        this.context = context;
    }
    
    /**
     * OPPO/OnePlusスタイラスAPIを初期化
     */
    public boolean initialize() {
        try {
            // MotionPredictorインスタンス作成
            motionPredictor = new MotionPredictor();
            
            // ディスプレイ情報を取得
            DisplayMetrics metrics = getDisplayMetrics();
            float dpiX = metrics.xdpi;
            float dpiY = metrics.ydpi;
            
            // リフレッシュレートを取得
            refreshRate = getDisplayRefreshRate();
            
            // 設定を適用
            motionPredictor.setDpi(dpiX, dpiY);
            motionPredictor.setRefreshRate(refreshRate);
            motionPredictor.setMaxPredictTime(maxPredictTime);
            motionPredictor.reset();
            
            isInitialized = true;
            Log.d(TAG, "OPPO Stylus initialized: DPI(" + dpiX + "," + dpiY + 
                  "), RefreshRate=" + refreshRate + "Hz");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OPPO Stylus API", e);
            return false;
        }
    }
    
    /**
     * ディスプレイメトリクスを取得
     */
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics;
    }
    
    /**
     * ディスプレイのリフレッシュレートを取得
     */
    private float getDisplayRefreshRate() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            return display.getRefreshRate();
        }
        return 60.0f; // デフォルト値
    }
    
    /**
     * タッチイベントを処理し、予測ポイントを取得
     * @return 予測されたタッチポイント（nullの場合は予測なし）
     */
    public TouchPointInfo processTouchEvent(MotionEvent event) {
        if (!isInitialized || motionPredictor == null) {
            return null;
        }
        
        try {
            // MotionEventからタッチ情報を抽出
            float x = event.getX();
            float y = event.getY();
            float pressure = event.getPressure();
            
            // 傾き角度を取得（デバイスがサポートしている場合）
            float tilt = 0.0f;
            if (event.getAxisValue(MotionEvent.AXIS_TILT) != 0) {
                tilt = event.getAxisValue(MotionEvent.AXIS_TILT);
            }
            
            long timestamp = event.getEventTime();
            
            // TouchPointInfoを作成
            TouchPointInfo currentPoint = new TouchPointInfo(x, y, pressure, tilt, timestamp);
            
            // 現在のポイントをプッシュ
            motionPredictor.pushTouchPoint(currentPoint);
            
            // 予測ポイントを取得
            TouchPointInfo predictedPoint = motionPredictor.predictTouchPoint();
            
            if (predictedPoint != null) {
                Log.v(TAG, String.format("Predicted: x=%.2f, y=%.2f, p=%.2f", 
                      predictedPoint.x, predictedPoint.y, predictedPoint.pressure));
            }
            
            return predictedPoint;
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing touch event", e);
            return null;
        }
    }
    
    /**
     * リセット（新しいストローク開始時など）
     */
    public void reset() {
        if (isInitialized && motionPredictor != null) {
            motionPredictor.reset();
            Log.d(TAG, "Motion predictor reset");
        }
    }
    
    /**
     * リフレッシュレートを更新
     */
    public void updateRefreshRate(float rate) {
        this.refreshRate = rate;
        if (isInitialized && motionPredictor != null) {
            motionPredictor.setRefreshRate(rate);
            Log.d(TAG, "Refresh rate updated: " + rate + "Hz");
        }
    }
    
    /**
     * 予測時間を設定
     */
    public void setMaxPredictTime(float timeMs) {
        this.maxPredictTime = timeMs;
        if (isInitialized && motionPredictor != null) {
            motionPredictor.setMaxPredictTime(timeMs);
            Log.d(TAG, "Max predict time set: " + timeMs + "ms");
        }
    }
    
    /**
     * リソースを解放
     */
    public void destroy() {
        if (motionPredictor != null) {
            try {
                motionPredictor.destroy();
                Log.d(TAG, "Motion predictor destroyed");
            } catch (Exception e) {
                Log.e(TAG, "Error destroying motion predictor", e);
            }
            motionPredictor = null;
        }
        isInitialized = false;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
}
```

### 2. カスタムDrawViewでの使用例

```java
package com.example.drawapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.drawapp.stylus.OppoStylusHandler;
import com.oplusos.vfxsdk.forecast.TouchPointInfo;

public class OppoDrawView extends View {
    private Paint paint;
    private Path currentPath;
    private OppoStylusHandler stylusHandler;
    
    public OppoDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        // ペイント設定
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xFF000000); // 黒
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        
        currentPath = new Path();
        
        // OPPOスタイラスハンドラーを初期化
        stylusHandler = new OppoStylusHandler(context);
        stylusHandler.initialize();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float pressure = event.getPressure();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 新しいストローク開始
                stylusHandler.reset();
                currentPath.reset();
                currentPath.moveTo(x, y);
                
                // 筆圧に応じてストローク幅を調整
                paint.setStrokeWidth(pressure * 20f);
                break;
                
            case MotionEvent.ACTION_MOVE:
                // 予測ポイントを取得
                TouchPointInfo predicted = stylusHandler.processTouchEvent(event);
                
                if (predicted != null) {
                    // 予測ポイントを使用
                    currentPath.lineTo(predicted.x, predicted.y);
                    paint.setStrokeWidth(predicted.pressure * 20f);
                } else {
                    // フォールバック：通常のポイントを使用
                    currentPath.lineTo(x, y);
                    paint.setStrokeWidth(pressure * 20f);
                }
                break;
                
            case MotionEvent.ACTION_UP:
                currentPath.lineTo(x, y);
                break;
        }
        
        invalidate();
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(currentPath, paint);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (stylusHandler != null) {
            stylusHandler.destroy();
        }
    }
}
```

---

## Samsung S Pen実装

### 1. Samsung S Penハンドラー

```java
package com.example.drawapp.stylus;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.samsung.android.sdk.penremote.AirMotionEvent;
import com.samsung.android.sdk.penremote.ButtonEvent;
import com.samsung.android.sdk.penremote.SpenEvent;
import com.samsung.android.sdk.penremote.SpenEventListener;
import com.samsung.android.sdk.penremote.SpenRemote;
import com.samsung.android.sdk.penremote.SpenUnit;
import com.samsung.android.sdk.penremote.SpenUnitManager;

public class SamsungSPenHandler {
    private static final String TAG = "SamsungSPenHandler";
    private SpenRemote spenRemote;
    private SpenUnitManager spenUnitManager;
    private SPenEventCallback callback;
    private boolean isConnected = false;
    
    public interface SPenEventCallback {
        void onButtonDown();
        void onButtonUp();
        void onAirMotion(float deltaX, float deltaY);
        void onConnectionChanged(boolean connected);
    }
    
    public SamsungSPenHandler(SPenEventCallback callback) {
        this.callback = callback;
        this.spenRemote = SpenRemote.getInstance();
    }
    
    /**
     * S Penサービスに接続
     */
    public void connect(Activity activity) {
        if (activity == null) {
            Log.e(TAG, "Activity is null");
            return;
        }
        
        spenRemote.connect(activity, new SpenRemote.ConnectionResultCallback() {
            @Override
            public void onSuccess(SpenUnitManager manager) {
                spenUnitManager = manager;
                isConnected = true;
                Log.d(TAG, "S Pen connected successfully");
                
                // イベントリスナーを登録
                registerEventListeners();
                
                if (callback != null) {
                    callback.onConnectionChanged(true);
                }
            }
            
            @Override
            public void onFailure(int errorCode) {
                isConnected = false;
                Log.e(TAG, "S Pen connection failed: " + errorCode);
                
                if (callback != null) {
                    callback.onConnectionChanged(false);
                }
            }
        });
        
        // 接続状態変更リスナー
        spenRemote.setConnectionStateChangeListener(new SpenRemote.ConnectionStateChangeListener() {
            @Override
            public void onChange(int state) {
                boolean connected = (state == SpenRemote.State.CONNECTED);
                isConnected = connected;
                Log.d(TAG, "Connection state changed: " + (connected ? "Connected" : "Disconnected"));
                
                if (callback != null) {
                    callback.onConnectionChanged(connected);
                }
            }
        });
    }
    
    /**
     * S Penイベントリスナーを登録
     */
    private void registerEventListeners() {
        if (spenUnitManager == null) {
            return;
        }
        
        try {
            // ボタン機能がサポートされているかチェック
            if (spenRemote.isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)) {
                SpenUnit buttonUnit = spenUnitManager.getUnit(SpenUnitManager.TYPE_BUTTON);
                if (buttonUnit != null) {
                    buttonUnit.registerSpenEventListener(new SpenEventListener() {
                        @Override
                        public void onEvent(SpenEvent event) {
                            ButtonEvent buttonEvent = new ButtonEvent(event);
                            handleButtonEvent(buttonEvent);
                        }
                    });
                    Log.d(TAG, "Button event listener registered");
                }
            }
            
            // エアモーション機能がサポートされているかチェック
            if (spenRemote.isFeatureEnabled(SpenRemote.FEATURE_TYPE_AIR_MOTION)) {
                SpenUnit airMotionUnit = spenUnitManager.getUnit(SpenUnitManager.TYPE_AIR_MOTION);
                if (airMotionUnit != null) {
                    airMotionUnit.registerSpenEventListener(new SpenEventListener() {
                        @Override
                        public void onEvent(SpenEvent event) {
                            AirMotionEvent airMotionEvent = new AirMotionEvent(event);
                            handleAirMotionEvent(airMotionEvent);
                        }
                    });
                    Log.d(TAG, "Air motion event listener registered");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error registering event listeners", e);
        }
    }
    
    /**
     * ボタンイベント処理
     */
    private void handleButtonEvent(ButtonEvent event) {
        if (callback == null) {
            return;
        }
        
        switch (event.getAction()) {
            case ButtonEvent.ACTION_DOWN:
                Log.d(TAG, "S Pen button down");
                callback.onButtonDown();
                break;
                
            case ButtonEvent.ACTION_UP:
                Log.d(TAG, "S Pen button up");
                callback.onButtonUp();
                break;
        }
    }
    
    /**
     * エアモーションイベント処理
     */
    private void handleAirMotionEvent(AirMotionEvent event) {
        if (callback == null) {
            return;
        }
        
        float deltaX = event.getDeltaX();
        float deltaY = event.getDeltaY();
        
        Log.v(TAG, String.format("Air motion: dx=%.2f, dy=%.2f", deltaX, deltaY));
        callback.onAirMotion(deltaX, deltaY);
    }
    
    /**
     * 切断
     */
    public void disconnect(Context context) {
        if (spenRemote != null && isConnected) {
            spenRemote.disconnect(context);
            isConnected = false;
            Log.d(TAG, "S Pen disconnected");
        }
    }
    
    /**
     * 機能がサポートされているかチェック
     */
    public boolean isFeatureSupported(int featureType) {
        return spenRemote != null && spenRemote.isFeatureEnabled(featureType);
    }
    
    public boolean isConnected() {
        return isConnected;
    }
}
```

### 2. Activityでの使用例

```java
public class SamsungDrawActivity extends AppCompatActivity {
    private SamsungSPenHandler sPenHandler;
    private TextView statusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        
        statusText = findViewById(R.id.status_text);
        
        // S Penハンドラーを初期化
        sPenHandler = new SamsungSPenHandler(new SamsungSPenHandler.SPenEventCallback() {
            @Override
            public void onButtonDown() {
                // ボタン押下時の処理（例：ツール切り替え）
                Toast.makeText(SamsungDrawActivity.this, "S Pen Button Down", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onButtonUp() {
                // ボタン離された時の処理
            }
            
            @Override
            public void onAirMotion(float deltaX, float deltaY) {
                // エアモーション時の処理（例：カーソル移動）
                // moveCursor(deltaX, deltaY);
            }
            
            @Override
            public void onConnectionChanged(boolean connected) {
                runOnUiThread(() -> {
                    statusText.setText(connected ? "S Pen Connected" : "S Pen Disconnected");
                });
            }
        });
        
        // 接続
        sPenHandler.connect(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sPenHandler != null) {
            sPenHandler.disconnect(this);
        }
    }
}
```

---

## Huawei/Honor実装

### 1. Huawei/Honorストローク予測ハンドラー

```java
package com.example.drawapp.stylus;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import com.huawei.stylus.penengine.HwPenEngineManager;
import com.huawei.stylus.penengine.estimate.HwMotionEventInfo;
import com.huawei.stylus.penengine.estimate.HwMotionEventQueue;
import com.huawei.stylus.penengine.estimate.HwStrokeEstimate;
import com.hihonor.android.magicx.app.penengine.HnPenEngineManager;
import com.hihonor.android.magicx.app.penengine.estimate.HnMotionEventInfo;
import com.hihonor.android.magicx.app.penengine.estimate.HnMotionEventQueue;
import com.hihonor.android.magicx.app.penengine.estimate.HnStrokeEstimate;
import java.util.ArrayList;
import java.util.List;

public class HuaweiHonorStylusHandler {
    private static final String TAG = "HuaweiHonorStylus";
    private Context context;
    private boolean isHuawei;
    private boolean isHonor;
    private boolean isInitialized = false;
    
    // Huawei用
    private HwMotionEventQueue hwQueue;
    private List<HwMotionEventInfo> hwEstimatedEvents;
    
    // Honor用
    private HnMotionEventQueue hnQueue;
    private List<HnMotionEventInfo> hnEstimatedEvents;
    
    public HuaweiHonorStylusHandler(Context context) {
        this.context = context;
        detectDevice();
    }
    
    /**
     * デバイスタイプを検出
     */
    private void detectDevice() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        
        if (manufacturer.contains("huawei")) {
            isHuawei = true;
            Log.d(TAG, "Huawei device detected");
        } else if (manufacturer.contains("honor")) {
            isHonor = true;
            Log.d(TAG, "Honor device detected");
        }
    }
    
    /**
     * ペンエンジンを初期化
     */
    public boolean initialize() {
        try {
            if (isHuawei) {
                return initializeHuawei();
            } else if (isHonor) {
                return initializeHonor();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            return false;
        }
    }
    
    /**
     * Huaweiペンエンジンを初期化
     */
    private boolean initializeHuawei() {
        if (!HwPenEngineManager.isEngineRuntimeAvailable(context)) {
            Log.w(TAG, "Huawei Pen Engine not available");
            return false;
        }
        
        if (!HwStrokeEstimate.isFeatureEnable()) {
            Log.w(TAG, "Huawei Stroke Estimate not enabled");
            return false;
        }
        
        // リフレッシュレートを設定
        HwStrokeEstimate.setRefreshRate(90.0f);
        
        // キューと結果リストを初期化
        hwQueue = new HwMotionEventQueue();
        hwEstimatedEvents = new ArrayList<>();
        
        isInitialized = true;
        Log.d(TAG, "Huawei Pen Engine initialized");
        return true;
    }
    
    /**
     * Honorペンエンジンを初期化
     */
    private boolean initializeHonor() {
        if (!HnPenEngineManager.isEngineRuntimeAvailable(context)) {
            Log.w(TAG, "Honor Pen Engine not available");
            return false;
        }
        
        if (!HnStrokeEstimate.isFeatureEnable()) {
            Log.w(TAG, "Honor Stroke Estimate not enabled");
            return false;
        }
        
        // リフレッシュレートを設定
        HnStrokeEstimate.setRefreshRate(90.0f);
        
        // キューと結果リストを初期化
        hnQueue = new HnMotionEventQueue();
        hnEstimatedEvents = new ArrayList<>();
        
        isInitialized = true;
        Log.d(TAG, "Honor Pen Engine initialized");
        return true;
    }
    
    /**
     * タッチイベントを処理し、予測イベントを取得
     */
    public List<EstimatedPoint> processTouchEvent(MotionEvent event) {
        if (!isInitialized) {
            return null;
        }
        
        if (isHuawei) {
            return processHuaweiEvent(event);
        } else if (isHonor) {
            return processHonorEvent(event);
        }
        
        return null;
    }
    
    /**
     * Huaweiイベント処理
     */
    private List<EstimatedPoint> processHuaweiEvent(MotionEvent event) {
        try {
            // MotionEventからイベント情報を作成
            float x = event.getX();
            float y = event.getY();
            float pressure = event.getPressure();
            float tilt = event.getAxisValue(MotionEvent.AXIS_TILT);
            long eventTime = event.getEventTime();
            
            HwMotionEventInfo eventInfo = new HwMotionEventInfo(x, y, pressure, tilt, eventTime);
            
            // キューに追加
            hwQueue.add(eventInfo);
            
            // 予測イベントを取得
            hwEstimatedEvents.clear();
            int result = HwStrokeEstimate.getEstimateEvent(hwQueue, hwEstimatedEvents);
            
            if (result >= 0 && !hwEstimatedEvents.isEmpty()) {
                List<EstimatedPoint> points = new ArrayList<>();
                for (HwMotionEventInfo estimated : hwEstimatedEvents) {
                    points.add(new EstimatedPoint(
                        estimated.getX(),
                        estimated.getY(),
                        estimated.getPress(),
                        estimated.getTilt()
                    ));
                }
                return points;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing Huawei event", e);
        }
        
        return null;
    }
    
    /**
     * Honorイベント処理
     */
    private List<EstimatedPoint> processHonorEvent(MotionEvent event) {
        try {
            // MotionEventからイベント情報を作成
            float x = event.getX();
            float y = event.getY();
            float pressure = event.getPressure();
            float tilt = event.getAxisValue(MotionEvent.AXIS_TILT);
            long eventTime = event.getEventTime();
            
            HnMotionEventInfo eventInfo = new HnMotionEventInfo(x, y, pressure, tilt, eventTime);
            
            // キューに追加
            hnQueue.add(eventInfo);
            
            // 予測イベントを取得
            hnEstimatedEvents.clear();
            int result = HnStrokeEstimate.getEstimateEvent(hnQueue, hnEstimatedEvents);
            
            if (result >= 0 && !hnEstimatedEvents.isEmpty()) {
                List<EstimatedPoint> points = new ArrayList<>();
                for (HnMotionEventInfo estimated : hnEstimatedEvents) {
                    points.add(new EstimatedPoint(
                        estimated.getX(),
                        estimated.getY(),
                        estimated.getPress(),
                        estimated.getTilt()
                    ));
                }
                return points;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing Honor event", e);
        }
        
        return null;
    }
    
    /**
     * リフレッシュレートを更新
     */
    public void setRefreshRate(float rate) {
        if (isHuawei) {
            HwStrokeEstimate.setRefreshRate(rate);
        } else if (isHonor) {
            HnStrokeEstimate.setRefreshRate(rate);
        }
        Log.d(TAG, "Refresh rate set to " + rate + "Hz");
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 予測ポイントを表すクラス
     */
    public static class EstimatedPoint {
        public final float x;
        public final float y;
        public final float pressure;
        public final float tilt;
        
        public EstimatedPoint(float x, float y, float pressure, float tilt) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
            this.tilt = tilt;
        }
    }
}
```

---

## SonarPen実装

### 1. SonarPenハンドラー

```java
package com.example.drawapp.stylus;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.greenbulb.sonarpen.SonarPenCallBack;
import com.greenbulb.sonarpen.SonarPenUtilities;

public class SonarPenHandler {
    private static final String TAG = "SonarPenHandler";
    private SonarPenUtilities sonarPen;
    private SonarPenCallback callback;
    private boolean isRunning = false;
    
    public interface SonarPenCallback {
        void onStatusChanged(int status);
        void onButtonPressed();
        void onPressureChanged(double pressure);
    }
    
    public SonarPenHandler(Context context, SonarPenCallback callback) {
        this.callback = callback;
        this.sonarPen = new SonarPenUtilities(context);
        setupCallbacks();
    }
    
    /**
     * コールバックを設定
     */
    private void setupCallbacks() {
        sonarPen.addSonarPenCallback(new SonarPenCallBack() {
            @Override
            public void onSonarPenStatusChange(int status) {
                Log.d(TAG, "Status changed: " + status);
                if (callback != null) {
                    callback.onStatusChanged(status);
                }
            }
            
            @Override
            public void onSonarPenButtonPressed() {
                Log.d(TAG, "Button pressed");
                if (callback != null) {
                    callback.onButtonPressed();
                }
            }
        });
    }
    
    /**
     * SonarPenを開始
     */
    public boolean start() {
        if (isRunning) {
            Log.w(TAG, "SonarPen already running");
            return true;
        }
        
        int result = sonarPen.start();
        
        if (result == 0) {
            isRunning = true;
            Log.d(TAG, "SonarPen started successfully");
            return true;
        } else {
            Log.e(TAG, "Failed to start SonarPen: " + result);
            return false;
        }
    }
    
    /**
     * SonarPenを停止
     */
    public void stop() {
        if (isRunning) {
            sonarPen.stop();
            isRunning = false;
            Log.d(TAG, "SonarPen stopped");
        }
    }
    
    /**
     * ビューにSonarPen機能を追加
     */
    public void attachToView(View view) {
        sonarPen.addSonarPenToView(view);
        Log.d(TAG, "SonarPen attached to view");
    }
    
    /**
     * タッチイベントを変換（筆圧情報を追加）
     */
    public MotionEvent translateTouchEvent(MotionEvent event) {
        MotionEvent translated = sonarPen.translateTouchEvent(event);
        
        // 現在の筆圧を取得
        double pressure = sonarPen.getCurrentPressure();
        if (callback != null) {
            callback.onPressureChanged(pressure);
        }
        
        return translated;
    }
    
    /**
     * 手動キャリブレーション値を設定
     */
    public void calibrate(float minValue, float maxValue) {
        sonarPen.saveManualReadings(minValue, maxValue);
        Log.d(TAG, String.format("Calibrated: min=%.2f, max=%.2f", minValue, maxValue));
    }
    
    /**
     * キャリブレーション画面を起動
     */
    public void startCalibration() {
        sonarPen.startCalibrateScreen();
    }
    
    /**
     * 筆圧スムージングを設定
     * @param option 0, 1, または 2
     */
    public void setPressureSmoothing(int option) {
        sonarPen.ChangePressureSmoothOption(option);
    }
    
    /**
     * タッチサイズを使用するか設定
     */
    public void setUseTouchSize(boolean use) {
        sonarPen.setUseTouchSize(use);
    }
    
    /**
     * SonarPenが画面上にあるかチェック
     */
    public boolean isPenOnScreen() {
        return sonarPen.isSonarPenOnScreen();
    }
    
    /**
     * オーディオを一時停止
     */
    public void pauseAudio() {
        sonarPen.audioPause();
    }
    
    /**
     * オーディオを再開
     */
    public void resumeAudio() {
        sonarPen.audioResume();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
```

### 2. SonarPen対応DrawView

```java
public class SonarPenDrawView extends View {
    private SonarPenHandler sonarPenHandler;
    private Paint paint;
    private Path currentPath;
    private float currentPressure = 0f;
    
    public SonarPenDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        
        currentPath = new Path();
        
        // SonarPenハンドラーを初期化
        sonarPenHandler = new SonarPenHandler(context, new SonarPenHandler.SonarPenCallback() {
            @Override
            public void onStatusChanged(int status) {
                // ステータス変更処理
            }
            
            @Override
            public void onButtonPressed() {
                // ボタン押下処理
                // 例：色を変更、ツールを切り替え
            }
            
            @Override
            public void onPressureChanged(double pressure) {
                currentPressure = (float) pressure;
            }
        });
        
        // ビューに接続
        sonarPenHandler.attachToView(this);
        
        // SonarPenを開始
        sonarPenHandler.start();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // SonarPenでイベントを変換（筆圧情報が追加される）
        MotionEvent translatedEvent = sonarPenHandler.translateTouchEvent(event);
        
        float x = translatedEvent.getX();
        float y = translatedEvent.getY();
        
        switch (translatedEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.reset();
                currentPath.moveTo(x, y);
                break;
                
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                // 筆圧に応じてストローク幅を調整
                paint.setStrokeWidth(Math.max(2f, currentPressure * 30f));
                break;
                
            case MotionEvent.ACTION_UP:
                currentPath.lineTo(x, y);
                break;
        }
        
        invalidate();
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(currentPath, paint);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sonarPenHandler != null) {
            sonarPenHandler.stop();
        }
    }
    
    // キャリブレーション用メソッド
    public void openCalibration() {
        if (sonarPenHandler != null) {
            sonarPenHandler.startCalibration();
        }
    }
}
```

---

## マルチベンダー統合マネージャー

### 完全な統合マネージャークラス

```java
package com.example.drawapp.stylus;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;

/**
 * マルチベンダーのスタイラスAPIを統合管理するクラス
 * デバイスを自動検出し、適切なAPIを使用
 */
public class UnifiedStylusManager {
    private static final String TAG = "UnifiedStylusManager";
    
    // ベンダータイプ
    public enum VendorType {
        OPPO,
        ONEPLUS,
        SAMSUNG,
        HUAWEI,
        HONOR,
        SONARPEN,  // フォールバック
        UNKNOWN
    }
    
    private Context context;
    private VendorType vendorType;
    private StylusEventCallback eventCallback;
    
    // 各ベンダーのハンドラー
    private OppoStylusHandler oppoHandler;
    private SamsungSPenHandler samsungHandler;
    private HuaweiHonorStylusHandler huaweiHonorHandler;
    private SonarPenHandler sonarPenHandler;
    
    private boolean isInitialized = false;
    
    /**
     * スタイラスイベントコールバック
     */
    public interface StylusEventCallback {
        void onPredictedPoint(float x, float y, float pressure, float tilt);
        void onButtonPressed();
        void onButtonReleased();
        void onAirMotion(float deltaX, float deltaY);
        void onConnectionChanged(boolean connected);
        void onError(String message);
    }
    
    public UnifiedStylusManager(Context context, StylusEventCallback callback) {
        this.context = context.getApplicationContext();
        this.eventCallback = callback;
        this.vendorType = detectVendor();
    }
    
    /**
     * デバイスベンダーを自動検出
     */
    private VendorType detectVendor() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String brand = Build.BRAND.toLowerCase();
        
        Log.d(TAG, "Detecting device: manufacturer=" + manufacturer + ", brand=" + brand);
        
        if (manufacturer.contains("oppo") || brand.contains("oppo")) {
            Log.d(TAG, "OPPO device detected");
            return VendorType.OPPO;
        } else if (manufacturer.contains("oneplus") || brand.contains("oneplus")) {
            Log.d(TAG, "OnePlus device detected");
            return VendorType.ONEPLUS;
        } else if (manufacturer.contains("samsung")) {
            Log.d(TAG, "Samsung device detected");
            return VendorType.SAMSUNG;
        } else if (manufacturer.contains("huawei")) {
            Log.d(TAG, "Huawei device detected");
            return VendorType.HUAWEI;
        } else if (manufacturer.contains("honor")) {
            Log.d(TAG, "Honor device detected");
            return VendorType.HONOR;
        }
        
        Log.d(TAG, "Unknown device, will use SonarPen as fallback");
        return VendorType.SONARPEN;
    }
    
    /**
     * スタイラスマネージャーを初期化
     */
    public boolean initialize(Activity activity) {
        Log.d(TAG, "Initializing for vendor: " + vendorType);
        
        try {
            switch (vendorType) {
                case OPPO:
                case ONEPLUS:
                    return initializeOppo();
                    
                case SAMSUNG:
                    return initializeSamsung(activity);
                    
                case HUAWEI:
                case HONOR:
                    return initializeHuaweiHonor();
                    
                case SONARPEN:
                default:
                    return initializeSonarPen();
            }
        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            if (eventCallback != null) {
                eventCallback.onError("Failed to initialize: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * OPPO/OnePlusを初期化
     */
    private boolean initializeOppo() {
        try {
            oppoHandler = new OppoStylusHandler(context);
            boolean success = oppoHandler.initialize();
            
            if (success) {
                isInitialized = true;
                Log.d(TAG, "OPPO/OnePlus stylus initialized successfully");
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize OPPO API, falling back to SonarPen", e);
            vendorType = VendorType.SONARPEN;
            return initializeSonarPen();
        }
    }
    
    /**
     * Samsungを初期化
     */
    private boolean initializeSamsung(Activity activity) {
        try {
            samsungHandler = new SamsungSPenHandler(new SamsungSPenHandler.SPenEventCallback() {
                @Override
                public void onButtonDown() {
                    if (eventCallback != null) {
                        eventCallback.onButtonPressed();
                    }
                }
                
                @Override
                public void onButtonUp() {
                    if (eventCallback != null) {
                        eventCallback.onButtonReleased();
                    }
                }
                
                @Override
                public void onAirMotion(float deltaX, float deltaY) {
                    if (eventCallback != null) {
                        eventCallback.onAirMotion(deltaX, deltaY);
                    }
                }
                
                @Override
                public void onConnectionChanged(boolean connected) {
                    if (eventCallback != null) {
                        eventCallback.onConnectionChanged(connected);
                    }
                }
            });
            
            samsungHandler.connect(activity);
            isInitialized = true;
            Log.d(TAG, "Samsung S Pen initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Samsung API, falling back to SonarPen", e);
            vendorType = VendorType.SONARPEN;
            return initializeSonarPen();
        }
    }
    
    /**
     * Huawei/Honorを初期化
     */
    private boolean initializeHuaweiHonor() {
        try {
            huaweiHonorHandler = new HuaweiHonorStylusHandler(context);
            boolean success = huaweiHonorHandler.initialize();
            
            if (success) {
                isInitialized = true;
                Log.d(TAG, "Huawei/Honor stylus initialized successfully");
                return true;
            } else {
                Log.w(TAG, "Huawei/Honor API not available, falling back to SonarPen");
                vendorType = VendorType.SONARPEN;
                return initializeSonarPen();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Huawei/Honor API, falling back to SonarPen", e);
            vendorType = VendorType.SONARPEN;
            return initializeSonarPen();
        }
    }
    
    /**
     * SonarPenを初期化（フォールバック）
     */
    private boolean initializeSonarPen() {
        try {
            sonarPenHandler = new SonarPenHandler(context, new SonarPenHandler.SonarPenCallback() {
                @Override
                public void onStatusChanged(int status) {
                    // ステータス処理
                }
                
                @Override
                public void onButtonPressed() {
                    if (eventCallback != null) {
                        eventCallback.onButtonPressed();
                    }
                }
                
                @Override
                public void onPressureChanged(double pressure) {
                    // 筆圧変化処理
                }
            });
            
            boolean success = sonarPenHandler.start();
            
            if (success) {
                isInitialized = true;
                Log.d(TAG, "SonarPen initialized successfully");
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SonarPen", e);
            return false;
        }
    }
    
    /**
     * タッチイベントを処理
     */
    public void processTouchEvent(MotionEvent event) {
        if (!isInitialized) {
            return;
        }
        
        try {
            switch (vendorType) {
                case OPPO:
                case ONEPLUS:
                    processOppoEvent(event);
                    break;
                    
                case SAMSUNG:
                    // Samsungはイベントリスナーで処理
                    break;
                    
                case HUAWEI:
                case HONOR:
                    processHuaweiHonorEvent(event);
                    break;
                    
                case SONARPEN:
                    processSonarPenEvent(event);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing touch event", e);
        }
    }
    
    private void processOppoEvent(MotionEvent event) {
        if (oppoHandler != null && eventCallback != null) {
            com.oplusos.vfxsdk.forecast.TouchPointInfo predicted = 
                oppoHandler.processTouchEvent(event);
            
            if (predicted != null) {
                eventCallback.onPredictedPoint(
                    predicted.x,
                    predicted.y,
                    predicted.pressure,
                    predicted.axisTilt
                );
            }
        }
    }
    
    private void processHuaweiHonorEvent(MotionEvent event) {
        if (huaweiHonorHandler != null && eventCallback != null) {
            List<HuaweiHonorStylusHandler.EstimatedPoint> points = 
                huaweiHonorHandler.processTouchEvent(event);
            
            if (points != null && !points.isEmpty()) {
                for (HuaweiHonorStylusHandler.EstimatedPoint point : points) {
                    eventCallback.onPredictedPoint(
                        point.x,
                        point.y,
                        point.pressure,
                        point.tilt
                    );
                }
            }
        }
    }
    
    private void processSonarPenEvent(MotionEvent event) {
        if (sonarPenHandler != null) {
            // SonarPenはイベントを変換
            MotionEvent translated = sonarPenHandler.translateTouchEvent(event);
            // 変換されたイベントは通常通り処理
        }
    }
    
    /**
     * ビューにアタッチ（SonarPenの場合）
     */
    public void attachToView(View view) {
        if (vendorType == VendorType.SONARPEN && sonarPenHandler != null) {
            sonarPenHandler.attachToView(view);
        }
    }
    
    /**
     * リセット
     */
    public void reset() {
        if (oppoHandler != null) {
            oppoHandler.reset();
        }
    }
    
    /**
     * リソースを解放
     */
    public void destroy() {
        try {
            if (oppoHandler != null) {
                oppoHandler.destroy();
                oppoHandler = null;
            }
            
            if (samsungHandler != null) {
                samsungHandler.disconnect(context);
                samsungHandler = null;
            }
            
            if (sonarPenHandler != null) {
                sonarPenHandler.stop();
                sonarPenHandler = null;
            }
            
            isInitialized = false;
            Log.d(TAG, "Stylus manager destroyed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during destroy", e);
        }
    }
    
    // ゲッター
    public VendorType getVendorType() {
        return vendorType;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
}
```

---

## カスタムDrawViewの実装

### 統合スタイラス対応DrawView

```java
package com.example.drawapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.drawapp.stylus.UnifiedStylusManager;
import java.util.ArrayList;
import java.util.List;

/**
 * すべてのスタイラスベンダーに対応したDrawView
 */
public class UnifiedDrawView extends View {
    private static final String TAG = "UnifiedDrawView";
    
    // 描画管理
    private List<Stroke> strokes = new ArrayList<>();
    private Stroke currentStroke;
    private Paint paint;
    
    // スタイラスマネージャー
    private UnifiedStylusManager stylusManager;
    
    // 設定
    private int currentColor = Color.BLACK;
    private float baseStrokeWidth = 5f;
    private boolean usePressure = true;
    
    public UnifiedDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        // ペイント設定
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(baseStrokeWidth);
    }
    
    /**
     * スタイラスマネージャーを設定
     */
    public void setStylusManager(UnifiedStylusManager manager) {
        this.stylusManager = manager;
        
        // SonarPenの場合はビューにアタッチ
        if (manager != null) {
            manager.attachToView(this);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float pressure = event.getPressure();
        
        // スタイラスマネージャーに通知
        if (stylusManager != null) {
            stylusManager.processTouchEvent(event);
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(x, y, pressure);
                break;
                
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(x, y, pressure);
                break;
                
            case MotionEvent.ACTION_UP:
                handleTouchUp(x, y, pressure);
                break;
        }
        
        invalidate();
        return true;
    }
    
    /**
     * タッチダウン処理
     */
    private void handleTouchDown(float x, float y, float pressure) {
        // 新しいストローク開始
        currentStroke = new Stroke(currentColor);
        currentStroke.addPoint(x, y, pressure);
        
        // スタイラスマネージャーをリセット
        if (stylusManager != null) {
            stylusManager.reset();
        }
    }
    
    /**
     * タッチムーブ処理
     */
    private void handleTouchMove(float x, float y, float pressure) {
        if (currentStroke != null) {
            currentStroke.addPoint(x, y, pressure);
        }
    }
    
    /**
     * タッチアップ処理
     */
    private void handleTouchUp(float x, float y, float pressure) {
        if (currentStroke != null) {
            currentStroke.addPoint(x, y, pressure);
            strokes.add(currentStroke);
            currentStroke = null;
        }
    }
    
    /**
     * 予測ポイントを処理（スタイラスマネージャーから呼ばれる）
     */
    public void onPredictedPoint(float x, float y, float pressure, float tilt) {
        if (currentStroke != null) {
            // 予測ポイントを追加（オプション：通常のポイントと区別することも可能）
            currentStroke.addPoint(x, y, pressure);
            invalidate();
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // すべてのストロークを描画
        for (Stroke stroke : strokes) {
            stroke.draw(canvas, paint, baseStrokeWidth, usePressure);
        }
        
        // 現在のストロークを描画
        if (currentStroke != null) {
            currentStroke.draw(canvas, paint, baseStrokeWidth, usePressure);
        }
    }
    
    /**
     * クリア
     */
    public void clear() {
        strokes.clear();
        currentStroke = null;
        invalidate();
    }
    
    /**
     * Undo（最後のストロークを削除）
     */
    public void undo() {
        if (!strokes.isEmpty()) {
            strokes.remove(strokes.size() - 1);
            invalidate();
        }
    }
    
    // セッター
    public void setColor(int color) {
        this.currentColor = color;
    }
    
    public void setStrokeWidth(float width) {
        this.baseStrokeWidth = width;
    }
    
    public void setUsePressure(boolean use) {
        this.usePressure = use;
    }
    
    /**
     * ストロークを表すクラス
     */
    private static class Stroke {
        private Path path;
        private List<PointData> points;
        private int color;
        
        public Stroke(int color) {
            this.color = color;
            this.path = new Path();
            this.points = new ArrayList<>();
        }
        
        public void addPoint(float x, float y, float pressure) {
            if (points.isEmpty()) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            points.add(new PointData(x, y, pressure));
        }
        
        public void draw(Canvas canvas, Paint paint, float baseWidth, boolean usePressure) {
            int oldColor = paint.getColor();
            float oldWidth = paint.getStrokeWidth();
            
            paint.setColor(color);
            
            if (usePressure && !points.isEmpty()) {
                // 筆圧を考慮して描画
                for (int i = 1; i < points.size(); i++) {
                    PointData prev = points.get(i - 1);
                    PointData curr = points.get(i);
                    
                    // 筆圧に応じて幅を調整
                    float avgPressure = (prev.pressure + curr.pressure) / 2f;
                    paint.setStrokeWidth(baseWidth * Math.max(0.1f, avgPressure));
                    
                    canvas.drawLine(prev.x, prev.y, curr.x, curr.y, paint);
                }
            } else {
                // 通常の描画
                paint.setStrokeWidth(baseWidth);
                canvas.drawPath(path, paint);
            }
            
            paint.setColor(oldColor);
            paint.setStrokeWidth(oldWidth);
        }
    }
    
    /**
     * ポイントデータ
     */
    private static class PointData {
        float x, y, pressure;
        
        PointData(float x, float y, float pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }
    }
}
```

---

## 完全なサンプルアプリケーション

### MainActivity

```java
package com.example.drawapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.drawapp.stylus.UnifiedStylusManager;
import com.example.drawapp.view.UnifiedDrawView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private UnifiedDrawView drawView;
    private UnifiedStylusManager stylusManager;
    private TextView statusText;
    private TextView pressureText;
    private SeekBar strokeWidthSeekBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // ビューを初期化
        drawView = findViewById(R.id.draw_view);
        statusText = findViewById(R.id.status_text);
        pressureText = findViewById(R.id.pressure_text);
        strokeWidthSeekBar = findViewById(R.id.stroke_width_seekbar);
        
        // スタイラスマネージャーを初期化
        initializeStylusManager();
        
        // UI設定
        setupUI();
    }
    
    /**
     * スタイラスマネージャーを初期化
     */
    private void initializeStylusManager() {
        stylusManager = new UnifiedStylusManager(this, 
            new UnifiedStylusManager.StylusEventCallback() {
                @Override
                public void onPredictedPoint(float x, float y, float pressure, float tilt) {
                    // 予測ポイントをDrawViewに渡す
                    drawView.onPredictedPoint(x, y, pressure, tilt);
                    
                    // 筆圧表示を更新
                    runOnUiThread(() -> {
                        pressureText.setText(String.format("Pressure: %.2f", pressure));
                    });
                }
                
                @Override
                public void onButtonPressed() {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Button Pressed", Toast.LENGTH_SHORT).show();
                        // ボタン押下時の処理（例：消しゴムに切り替え）
                    });
                }
                
                @Override
                public void onButtonReleased() {
                    // ボタン解放時の処理
                }
                
                @Override
                public void onAirMotion(float deltaX, float deltaY) {
                    // エアモーション処理
                }
                
                @Override
                public void onConnectionChanged(boolean connected) {
                    runOnUiThread(() -> {
                        String status = connected ? "Connected" : "Disconnected";
                        statusText.setText("Stylus: " + status);
                    });
                }
                
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error: " + message, 
                                     Toast.LENGTH_LONG).show();
                    });
                }
            });
        
        // 初期化
        boolean success = stylusManager.initialize(this);
        
        if (success) {
            // DrawViewにマネージャーを設定
            drawView.setStylusManager(stylusManager);
            
            // ステータス表示
            String vendor = stylusManager.getVendorType().toString();
            statusText.setText("Stylus API: " + vendor);
            Toast.makeText(this, "Using " + vendor + " API", Toast.LENGTH_LONG).show();
        } else {
            statusText.setText("Stylus: Not Available");
            Toast.makeText(this, "Failed to initialize stylus", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * UI設定
     */
    private void setupUI() {
        // FAB（クリアボタン）
        FloatingActionButton fab = findViewById(R.id.fab_clear);
        fab.setOnClickListener(v -> {
            drawView.clear();
            Toast.makeText(this, "Canvas cleared", Toast.LENGTH_SHORT).show();
        });
        
        // ストローク幅調整
        strokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float width = progress + 1f;  // 1-100
                drawView.setStrokeWidth(width);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_undo) {
            drawView.undo();
            return true;
        } else if (id == R.id.action_clear) {
            drawView.clear();
            return true;
        } else if (id == R.id.action_color_black) {
            drawView.setColor(0xFF000000);
            return true;
        } else if (id == R.id.action_color_red) {
            drawView.setColor(0xFFFF0000);
            return true;
        } else if (id == R.id.action_color_blue) {
            drawView.setColor(0xFF0000FF);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stylusManager != null) {
            stylusManager.destroy();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // SonarPenの場合、オーディオを一時停止することも可能
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // SonarPenの場合、オーディオを再開
    }
}
```

### レイアウトXML (activity_main.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="?attr/colorPrimary"
            app:popupTheme="@style/ThemeOverlay.AppCompat.Light" />

    </com.google.android.material.appbar.AppBarLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <!-- ステータス表示 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="8dp"
            android:background="#F5F5F5">

            <TextView
                android:id="@+id/status_text"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Stylus: Initializing..."
                android:textSize="14sp" />

            <TextView
                android:id="@+id/pressure_text"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Pressure: 0.00"
                android:textSize="14sp" />
        </LinearLayout>

        <!-- 描画エリア -->
        <com.example.drawapp.view.UnifiedDrawView
            android:id="@+id/draw_view"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:background="#FFFFFF" />

        <!-- コントロール -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp"
            android:gravity="center_vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Stroke Width:"
                android:layout_marginEnd="8dp" />

            <SeekBar
                android:id="@+id/stroke_width_seekbar"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:max="100"
                android:progress="5" />
        </LinearLayout>
    </LinearLayout>

    <!-- FAB -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_clear"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        app:srcCompat="@android:drawable/ic_menu_delete" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.drawapp">

    <!-- 必要な権限 -->
    <!-- SonarPen用 -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    
    <!-- Samsung S Pen用 -->
    <uses-permission android:name="com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.DrawApp">
        
        <activity
            android:name=".MainActivity"
            android:screenOrientation="portrait"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- OPPO/OnePlus ボタンイベント受信 -->
        <receiver android:name=".receiver.OppoStylusButtonReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.oplus.ipemanager.action.PENCIL_DOUBLE_CLICK" />
            </intent-filter>
        </receiver>

        <!-- Huawei ボタンイベント受信 -->
        <receiver android:name=".receiver.HuaweiStylusButtonReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.huawei.stylus.action.BUTTON_DOUBLE_PRESSED" />
            </intent-filter>
        </receiver>

        <!-- Honor ボタンイベント受信 -->
        <receiver android:name=".receiver.HonorStylusButtonReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.hihonor.stylus.action.STYLUS_DOUBLE_CLICK" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
```

### build.gradle (app)

```gradle
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.drawapp"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // 各ベンダーのSDK（プロジェクトに応じて追加）
    // OPPO/OnePlus SDK
    // implementation files('libs/oppo-vfxsdk.aar')
    
    // Samsung S Pen SDK
    // implementation files('libs/samsung-penremote-sdk.aar')
    
    // SonarPen SDK
    // implementation files('libs/sonarpen-sdk.aar')
}
```

---

## まとめ

このドキュメントには、以下のすぐに使える実装が含まれています：

1. **ベンダー別ハンドラー**: OPPO、Samsung、Huawei/Honor、SonarPen
2. **統合マネージャー**: デバイス自動検出とマルチベンダー対応
3. **カスタムDrawView**: 筆圧対応の描画ビュー
4. **完全なサンプルアプリ**: MainActivity、レイアウト、マニフェスト

すべてのコードはコピー&ペーストで使用でき、プロジェクトに統合できます。

### 使用方法

1. 必要なSDK（.aarファイル）を`libs/`フォルダに配置
2. パッケージ名を変更（`com.example.drawapp` → あなたのパッケージ名）
3. 必要な権限を確認
4. ビルドして実行

**重要**: 各ベンダーのSDKライブラリは、それぞれのベンダーから入手する必要があります。

