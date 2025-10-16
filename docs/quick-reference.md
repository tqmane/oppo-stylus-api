# スタイラスAPI クイックリファレンス

すぐに使える簡潔なコード例集

---

## 🚀 最速スタート（3行のコード）

```java
UnifiedStylusManager manager = new UnifiedStylusManager(this, callback);
manager.initialize(this);
drawView.setStylusManager(manager);
```

---

## 📋 ベンダー別初期化

### OPPO/OnePlus

```java
OppoStylusHandler handler = new OppoStylusHandler(context);
handler.initialize();

// タッチイベント処理
TouchPointInfo predicted = handler.processTouchEvent(event);
if (predicted != null) {
    float x = predicted.x;
    float y = predicted.y;
    float pressure = predicted.pressure;
}
```

### Samsung S Pen

```java
SamsungSPenHandler handler = new SamsungSPenHandler(callback);
handler.connect(activity);

// コールバック実装
new SPenEventCallback() {
    @Override
    public void onButtonDown() { /* 処理 */ }
    @Override
    public void onAirMotion(float dx, float dy) { /* 処理 */ }
};
```

### Huawei/Honor

```java
HuaweiHonorStylusHandler handler = new HuaweiHonorStylusHandler(context);
handler.initialize();
handler.setRefreshRate(90.0f);

// 予測ポイント取得
List<EstimatedPoint> points = handler.processTouchEvent(event);
```

### SonarPen

```java
SonarPenHandler handler = new SonarPenHandler(context, callback);
handler.start();
handler.attachToView(view);

// イベント変換
MotionEvent translated = handler.translateTouchEvent(event);
```

---

## 🎨 DrawView統合

```java
public class MyDrawView extends View {
    private UnifiedStylusManager stylusManager;
    
    public void setStylusManager(UnifiedStylusManager manager) {
        this.stylusManager = manager;
        manager.attachToView(this);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (stylusManager != null) {
            stylusManager.processTouchEvent(event);
        }
        
        // 通常の描画処理
        float x = event.getX();
        float y = event.getY();
        float pressure = event.getPressure();
        
        // 描画...
        return true;
    }
}
```

---

## ⚙️ 設定

### リフレッシュレート設定

```java
// OPPO
oppoHandler.updateRefreshRate(120.0f);

// Huawei/Honor
huaweiHandler.setRefreshRate(90.0f);
```

### 予測時間設定（OPPO）

```java
oppoHandler.setMaxPredictTime(16.0f); // 16ms
```

### 筆圧スムージング（SonarPen）

```java
sonarPenHandler.setPressureSmoothing(2); // 0, 1, 2
```

---

## 🎯 イベント処理

### コールバック実装

```java
new StylusEventCallback() {
    @Override
    public void onPredictedPoint(float x, float y, float pressure, float tilt) {
        // 予測ポイントで描画
        drawPoint(x, y, pressure);
    }
    
    @Override
    public void onButtonPressed() {
        // ツール切り替えなど
        switchTool();
    }
    
    @Override
    public void onConnectionChanged(boolean connected) {
        // UI更新
        updateStatus(connected);
    }
};
```

---

## 🔄 ライフサイクル管理

```java
public class DrawActivity extends AppCompatActivity {
    private UnifiedStylusManager stylusManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        stylusManager = new UnifiedStylusManager(this, callback);
        stylusManager.initialize(this);
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
        // SonarPenの場合
        if (sonarPenHandler != null) {
            sonarPenHandler.pauseAudio();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (sonarPenHandler != null) {
            sonarPenHandler.resumeAudio();
        }
    }
}
```

---

## 📱 デバイス検出

```java
String manufacturer = Build.MANUFACTURER.toLowerCase();

if (manufacturer.contains("oppo") || manufacturer.contains("oneplus")) {
    // OPPO/OnePlus API
} else if (manufacturer.contains("samsung")) {
    // Samsung API
} else if (manufacturer.contains("huawei")) {
    // Huawei API
} else if (manufacturer.contains("honor")) {
    // Honor API
} else {
    // フォールバック
}
```

---

## 🛡️ エラーハンドリング

```java
try {
    boolean success = stylusManager.initialize(this);
    if (!success) {
        // フォールバックモード
        Toast.makeText(this, "Using fallback mode", Toast.LENGTH_SHORT).show();
    }
} catch (Exception e) {
    Log.e(TAG, "Initialization failed", e);
    // エラー処理
}
```

---

## 📦 必要な権限

### AndroidManifest.xml

```xml
<!-- SonarPen用 -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Samsung S Pen用 -->
<uses-permission android:name="com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE" />
```

### 実行時権限リクエスト（SonarPen）

```java
if (ContextCompat.checkSelfPermission(this, 
        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO}, 
            REQUEST_CODE_AUDIO);
}
```

---

## 🎨 筆圧対応描画

```java
@Override
protected void onDraw(Canvas canvas) {
    for (int i = 1; i < points.size(); i++) {
        PointData prev = points.get(i - 1);
        PointData curr = points.get(i);
        
        // 筆圧に応じて幅を調整
        float avgPressure = (prev.pressure + curr.pressure) / 2f;
        paint.setStrokeWidth(baseWidth * avgPressure);
        
        canvas.drawLine(prev.x, prev.y, curr.x, curr.y, paint);
    }
}
```

---

## 🔧 トラブルシューティング

### APIが初期化できない

```java
if (!handler.initialize()) {
    Log.e(TAG, "API not available on this device");
    // フォールバック処理
    useFallbackMode();
}
```

### 予測ポイントがnull

```java
TouchPointInfo predicted = handler.processTouchEvent(event);
if (predicted == null) {
    // 通常のポイントを使用
    useNormalPoint(event.getX(), event.getY(), event.getPressure());
}
```

### SonarPenの音声衝突

```java
@Override
protected void onPause() {
    super.onPause();
    sonarPenHandler.pauseAudio(); // 他のオーディオ再生時
}

@Override
protected void onResume() {
    super.onResume();
    sonarPenHandler.resumeAudio(); // 戻ってきたら再開
}
```

---

## 📊 パフォーマンス最適化

### イベントフィルタリング

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    // ツールタイプをチェック
    int toolType = event.getToolType(0);
    if (toolType == MotionEvent.TOOL_TYPE_STYLUS) {
        // スタイラスのみ処理
        stylusManager.processTouchEvent(event);
    }
    return true;
}
```

### バッチ処理

```java
private List<TouchPointInfo> pointBuffer = new ArrayList<>();

public void processTouchEvent(MotionEvent event) {
    TouchPointInfo point = createPoint(event);
    pointBuffer.add(point);
    
    if (pointBuffer.size() >= BATCH_SIZE) {
        processBatch(pointBuffer);
        pointBuffer.clear();
    }
}
```

---

## 🎯 よく使うパターン

### ツール切り替え

```java
private Tool currentTool = Tool.PEN;

@Override
public void onButtonPressed() {
    currentTool = (currentTool == Tool.PEN) ? Tool.ERASER : Tool.PEN;
    updateToolUI(currentTool);
}
```

### Undo/Redo

```java
private Stack<Stroke> strokes = new Stack<>();
private Stack<Stroke> redoStack = new Stack<>();

public void undo() {
    if (!strokes.isEmpty()) {
        Stroke stroke = strokes.pop();
        redoStack.push(stroke);
        invalidate();
    }
}

public void redo() {
    if (!redoStack.isEmpty()) {
        Stroke stroke = redoStack.pop();
        strokes.push(stroke);
        invalidate();
    }
}
```

### 色選択

```java
private int[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN};
private int currentColorIndex = 0;

@Override
public void onButtonPressed() {
    currentColorIndex = (currentColorIndex + 1) % colors.length;
    paint.setColor(colors[currentColorIndex]);
}
```

---

## 📱 レスポンシブ設定

### DPI対応

```java
DisplayMetrics metrics = getResources().getDisplayMetrics();
float dpi = metrics.densityDpi;

// DPIに応じてストローク幅を調整
float scaledWidth = baseWidth * (dpi / 160f);
paint.setStrokeWidth(scaledWidth);
```

### リフレッシュレート自動検出

```java
WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
float refreshRate = wm.getDefaultDisplay().getRefreshRate();

// リフレッシュレートに応じて設定
if (refreshRate >= 120f) {
    predictor.setMaxPredictTime(8.0f);  // 高速
} else if (refreshRate >= 90f) {
    predictor.setMaxPredictTime(11.0f);
} else {
    predictor.setMaxPredictTime(16.0f); // 標準
}
```

---

## 🔗 リンク

- **[完全な実装ガイド](./implementation-guide.md)** - 詳細なコードと説明
- **[API仕様書](./stylus-api-documentation.md)** - 技術詳細
- **[プロジェクトREADME](./README.md)** - 概要と目次

---

**クイックリファレンス v1.0**  
**更新日**: 2025年10月16日

