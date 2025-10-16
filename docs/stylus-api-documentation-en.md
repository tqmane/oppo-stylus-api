# Stylus API Analysis Documentation

**App**: HiPaint (com.aige.hipaint) v5.0.10-188  
**Analysis Date**: October 16, 2025

## Table of Contents

1. [Overview](#overview)
2. [OPPO/OnePlus Stylus API](#oppooneplus-stylus-api)
3. [Samsung S Pen Remote API](#samsung-s-pen-remote-api)
4. [Huawei Stylus Pen Engine API](#huawei-stylus-pen-engine-api)
5. [Honor Pen Engine API](#honor-pen-engine-api)
6. [SonarPen API](#sonarpen-api)
7. [Summary](#summary)

---

## Overview

The HiPaint app integrates multiple stylus vendor APIs to support pen input functionality across various devices. This document analyzes the details of each vendor's stylus API, with a particular focus on OPPO/OnePlus APIs.

**Supported Stylus Vendors**:
- OPPO/OnePlus (OPlusOS VFX SDK)
- Samsung (S Pen Remote SDK)
- Huawei (Stylus Pen Engine)
- Honor (MagicX Pen Engine)
- SonarPen (Third-party acoustic-based)

---

## OPPO/OnePlus Stylus API

### Package
`com.oplusos.vfxsdk.forecast`

### Overview

The OPPO/OnePlus Stylus API provides **Motion Prediction** functionality to minimize pen input latency and deliver a more natural drawing experience. This SDK predicts touch point trajectories to improve synchronization with refresh rates.

### Key Classes

#### 1. MotionPredictor

**File**: `com/oplusos/vfxsdk/forecast/MotionPredictor.java`

**Description**: Main class for managing motion prediction of touch events.

**Constants**:
- `VERSION`: "VERSION_2.0.0"
- `TAG`: "Forecast:MotionPredictor"

**Key Methods**:

```java
// Constructor
public MotionPredictor()

// Create native handle
void create()

// Set refresh rate (Hz)
public void setRefreshRate(float refreshRate)

// Set screen DPI
public void setDpi(float dpiX, float dpiY)

// Set maximum prediction time (milliseconds)
public void setMaxPredictTime(float maxPredictTime)

// Reset prediction system
public void reset()

// Push touch point to queue
public void pushTouchPoint(TouchPointInfo touchPointInfo)

// Predict next touch point
public TouchPointInfo predictTouchPoint()

// Release resources
public void destroy()
```

**Usage Example**:
```java
MotionPredictor predictor = new MotionPredictor();
predictor.setRefreshRate(120.0f);  // 120Hz display
predictor.setDpi(440.0f, 440.0f);
predictor.setMaxPredictTime(16.0f); // 16ms prediction

// On touch event received
TouchPointInfo currentPoint = new TouchPointInfo(x, y, pressure, tilt, timestamp);
predictor.pushTouchPoint(currentPoint);

// Get predicted point
TouchPointInfo predictedPoint = predictor.predictTouchPoint();
```

#### 2. TouchPointInfo

**File**: `com/oplusos/vfxsdk/forecast/TouchPointInfo.java`

**Description**: Class holding touch point data.

**Fields**:
```java
public float x;           // X coordinate
public float y;           // Y coordinate
public float pressure;    // Pressure (0.0 ~ 1.0)
public float axisTilt;    // Pen tilt angle
public long timestamp;    // Timestamp (milliseconds)
```

**Constructors**:
```java
// Constructor with parameters
public TouchPointInfo(float x, float y, float pressure, float axisTilt, long timestamp)

// Default constructor (all initialized to 0)
public TouchPointInfo()
```

#### 3. NativeForecast

**File**: `com/oplusos/vfxsdk/forecast/NativeForecast.java`

**Description**: JNI interface to native library.

**Native Methods**:
```java
// Create native handle
public static native long create(long handle);

// Destroy prediction engine
public static native void destroy(long handle);

// Push touch point
public static native void pushTouchPoint(long handle, TouchPointInfo info);

// Get predicted touch point
public static native TouchPointInfo predictTouchPoint(long handle);

// Reset system
public static native void reset(long handle);

// Set refresh rate
public static native void setRefreshRate(long handle, float rate);

// Set DPI
public static native void setDpi(long handle, float dpiX, float dpiY);

// Set maximum prediction time
public static native void setMaxPredictTime(long handle, float maxTime);
```

**Loaded Library**:
```java
static {
    System.loadLibrary("forecast");
}
```

### Features and Advantages

1. **Low Latency Drawing**: Motion prediction minimizes pen input latency
2. **High Refresh Rate Support**: Optimized for displays up to 120Hz+
3. **Pressure & Tilt Support**: Detailed touch data including pressure and tilt angle
4. **Customizable**: Adjustable DPI, refresh rate, and prediction time

### Technical Details

- **Prediction Algorithm**: High-speed trajectory prediction via native C++ implementation
- **Timestamp Management**: Uses relative time for system clock synchronization
- **Memory Management**: Efficient resource management using native handles

### Integration on OPPO/OnePlus Devices

HiPaint automatically enables this API when OPPO/OnePlus devices are detected. The DrawMainUI class includes the following action definition:

```java
public static final String ACTION_OPPO_STYLUS_BUTTON_DOUBLE_PRESSED = 
    "com.oplus.ipemanager.action.PENCIL_DOUBLE_CLICK";
```

This also supports OPPO stylus double-click button events.

---

## Samsung S Pen Remote API

### Package
`com.samsung.android.sdk.penremote`

### Overview

The Samsung S Pen Remote SDK provides remote functionality (button events, air motion) for the S Pen on Galaxy Note series devices.

### Key Classes

#### 1. SpenRemote

**File**: `com/samsung/android/sdk/penremote/SpenRemote.java`

**Description**: Main class managing connection to S Pen remote service.

**Constants**:
- `SDK_VERSION`: 16777217 (Version 1.0.1)
- `FEATURE_BUTTON`: Button functionality
- `FEATURE_AIR_MOTION`: Air motion functionality
- `BIND_PACKAGE_NAME`: "com.samsung.android.service.aircommand"

**Key Methods**:
```java
// Get singleton instance
public static SpenRemote getInstance()

// Connect to S Pen service
public void connect(Context context, ConnectionResultCallback callback)

// Disconnect
public void disconnect(Context context)

// Check if feature is enabled
public boolean isFeatureEnabled(int featureType)

// Set connection state change listener
public void setConnectionStateChangeListener(ConnectionStateChangeListener listener)

// Check connection state
public boolean isConnected()

// Get version info
public String getVersionName()
public int getVersionCode()
```

**Interfaces**:
```java
// Connection result callback
public interface ConnectionResultCallback {
    void onSuccess(SpenUnitManager spenUnitManager);
    void onFailure(int errorCode);
}

// Connection state change listener
public interface ConnectionStateChangeListener {
    void onChange(int state);
}
```

**Error Codes**:
- `CONNECTION_FAILED` (-2): Connection failed
- `UNSUPPORTED_DEVICE` (-1): Unsupported device
- `UNKNOWN` (-100): Unknown error

**Connection States**:
- `CONNECTED` (0): Connected
- `DISCONNECTED` (-1): Disconnected
- `DISCONNECTED_BY_UNKNOWN_REASON` (-2): Disconnected for unknown reason

#### 2. ButtonEvent

**File**: `com/samsung/android/sdk/penremote/ButtonEvent.java`

**Description**: Class representing S Pen button events.

**Constants**:
```java
public static final int ACTION_DOWN = 0;
public static final int ACTION_UP = 1;
```

**Methods**:
```java
public int getAction()        // Get button action
public long getTimeStamp()    // Get timestamp
```

#### 3. AirMotionEvent

**File**: `com/samsung/android/sdk/penremote/AirMotionEvent.java`

**Description**: S Pen air motion (gesture in air) events.

**Fields**:
```java
public static final int DELTA_X = 0;
public static final int DELTA_Y = 1;
```

**Methods**:
```java
public float getDeltaX()      // X-axis movement
public float getDeltaY()      // Y-axis movement
public long getTimeStamp()    // Timestamp
```

#### 4. SpenEvent

**File**: `com/samsung/android/sdk/penremote/SpenEvent.java`

**Description**: Generic S Pen event base class.

**Event Types**:
```java
public static final int TYPE_BUTTON = 0;
public static final int TYPE_AIR_MOTION = 1;
```

**Methods**:
```java
public float getValue(int index)
public void setValue(int index, float value)
public long getTimeStamp()
public void setTimeStamp(long timestamp)
```

### Usage Example

```java
// Connect to S Pen service
SpenRemote spenRemote = SpenRemote.getInstance();
spenRemote.connect(this, new SpenRemote.ConnectionResultCallback() {
    @Override
    public void onSuccess(SpenUnitManager manager) {
        // Connection successful
        manager.registerSpenEventListener(new SpenEventListener() {
            @Override
            public void onButtonEvent(ButtonEvent event) {
                if (event.getAction() == ButtonEvent.ACTION_DOWN) {
                    // Handle button press
                }
            }
            
            @Override
            public void onAirMotionEvent(AirMotionEvent event) {
                float deltaX = event.getDeltaX();
                float deltaY = event.getDeltaY();
                // Handle air motion
            }
        });
    }
    
    @Override
    public void onFailure(int errorCode) {
        // Handle error
    }
});
```

### Required Permissions

```xml
<uses-permission android:name="com.samsung.android.sdk.penremote.BIND_SPEN_REMOTE"/>
```

---

## Huawei Stylus Pen Engine API

### Package
`com.huawei.stylus.penengine`

### Overview

Huawei's Stylus Pen Engine supports styluses like M-Pencil on Huawei devices and provides Stroke Estimate functionality.

### Key Classes

#### 1. HwPenEngineManager

**File**: `com/huawei/stylus/penengine/HwPenEngineManager.java`

**Description**: Manager class for checking pen engine availability.

**Methods**:
```java
// Check if pen engine runtime is available
public static boolean isEngineRuntimeAvailable(Context context)

// Check if specific feature is supported
public static boolean isSupportFeature(Context context, int featureLevel)

// Get API level
public static int getApiLevel()

// Check API level compatibility
public static boolean isApiLevelCompatible(int requiredLevel)
```

**Usage Example**:
```java
if (HwPenEngineManager.isEngineRuntimeAvailable(context)) {
    // Huawei pen engine is available
    if (HwPenEngineManager.isSupportFeature(context, 1)) {
        // Feature level 1 is supported
    }
}
```

#### 2. HwStrokeEstimate

**File**: `com/huawei/stylus/penengine/estimate/HwStrokeEstimate.java`

**Description**: Class providing stroke prediction functionality.

**Methods**:
```java
// Check if stroke estimation feature is enabled
public static synchronized boolean isFeatureEnable()

// Get predicted motion events
public static synchronized int getEstimateEvent(
    IHwRecycleQueue queue, 
    List<HwMotionEventInfo> eventList
)

// Set refresh rate
public static synchronized void setRefreshRate(float refreshRate)
```

#### 3. HwMotionEventInfo

**File**: `com/huawei/stylus/penengine/estimate/HwMotionEventInfo.java`

**Description**: Class holding motion event information.

**Fields and Methods**:
```java
// Constructor
public HwMotionEventInfo(float x, float y, float press, float tilt, long eventTime)

// Getters
public float getX()
public float getY()
public float getPress()      // Pressure
public float getTilt()       // Tilt
public long getEventTime()

// Setters
public void setX(float x)
public void setY(float y)
public void setPress(float press)
public void setTilt(float tilt)
public void setEventTime(long time)
```

### Usage Example

```java
// Initialize stroke estimation
if (HwStrokeEstimate.isFeatureEnable()) {
    HwStrokeEstimate.setRefreshRate(90.0f);  // 90Hz display
    
    // Prepare event queue
    HwMotionEventQueue queue = new HwMotionEventQueue();
    List<HwMotionEventInfo> estimatedEvents = new ArrayList<>();
    
    // Get predicted events
    int result = HwStrokeEstimate.getEstimateEvent(queue, estimatedEvents);
    
    if (result >= 0) {
        for (HwMotionEventInfo event : estimatedEvents) {
            float x = event.getX();
            float y = event.getY();
            float pressure = event.getPress();
            // Process predicted events
        }
    }
}
```

### Button Events

HiPaint app defines the following action:

```java
public static final String ACTION_HUAWEI_STYLUS_BUTTON_DOUBLE_PRESSED = 
    "com.huawei.stylus.action.BUTTON_DOUBLE_PRESSED";
```

---

## Honor Pen Engine API

### Package
`com.hihonor.android.magicx.app.penengine`

### Overview

Honor's Pen Engine API (MagicX Pen Engine) is very similar to Huawei's API but optimized for Honor brand devices. It also provides additional features like color picker functionality.

### Key Classes

#### 1. HnPenEngineManager

**File**: `com/hihonor/android/magicx/app/penengine/HnPenEngineManager.java`

**Description**: Honor pen engine management class.

**Methods**:
```java
// Check if pen engine runtime is available
public static boolean isEngineRuntimeAvailable(Context context)

// Check if double-click feature is supported
public static boolean isSupportDoubleClick(Context context)

// Check if color picker feature is available
public static boolean checkColorPickerAvailable(Context context)

// Show color picker
public static void showColorPicker(
    IHonorColorListener listener, 
    int initialColor, 
    Context context
)
```

**Features**:
- Similar stroke prediction functionality as Huawei API
- **Color Picker Feature**: Directly extract colors from screen using stylus
- **Double-Click Detection**: Native support for stylus button double-click

#### 2. HnStrokeEstimate

**File**: `com/hihonor/android/magicx/app/penengine/estimate/HnStrokeEstimate.java`

**Description**: Similar stroke prediction functionality as Huawei.

**Methods**:
```java
public static synchronized boolean isFeatureEnable()
public static synchronized int getEstimateEvent(
    IHnRecycleQueue queue, 
    List<HnMotionEventInfo> eventList
)
public static synchronized void setRefreshRate(float refreshRate)
```

#### 3. HnMotionEventInfo

**File**: `com/hihonor/android/magicx/app/penengine/estimate/HnMotionEventInfo.java`

**Description**: Same structure as HwMotionEventInfo.

**Fields**:
```java
public float mAxisX;
public float mAxisY;
public float mPress;
public float mTilt;
public long mEventTime;
```

### Color Picker Usage Example

```java
if (HnPenEngineManager.checkColorPickerAvailable(context)) {
    HnPenEngineManager.showColorPicker(
        new IHonorColorListener() {
            @Override
            public void onColorSelected(int color) {
                // Process selected color
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
            }
        },
        Color.WHITE,  // Initial color
        context
    );
}
```

### Button Events

```java
public static final String ACTION_HONOR_STYLUS_BUTTON_DOUBLE_PRESSED = 
    "com.hihonor.stylus.action.STYLUS_DOUBLE_CLICK";
```

---

## SonarPen API

### Package
`com.greenbulb.sonarpen`

### Overview

SonarPen is a unique third-party stylus solution that achieves pressure detection using acoustic signals. It adds pressure functionality to standard styluses or touch pens without requiring dedicated hardware.

### Technology Principle

- **Acoustic-Based**: Transmits ultrasonic signals from headphone jack or USB-C
- **Pressure Detection**: Acoustic signal amplitude changes according to stylus pressure on screen
- **Real-time Processing**: Captures acoustic signals from microphone using AudioRecord

### Key Classes

#### SonarPenUtilities

**File**: `com/greenbulb/sonarpen/SonarPenUtilities.java`

**Description**: Main control class for SonarPen (2000+ line large-scale class).

**Version**: "3.11 (2024042101)"

**Key Methods**:

```java
// Constructor
public SonarPenUtilities(Context context)

// Start SonarPen
public int start()

// Stop SonarPen
public void stop()

// Add SonarPen functionality to view
public void addSonarPenToView(View view)

// Set callback
public void addSonarPenCallback(SonarPenCallBack callback)

// Translate touch event
public MotionEvent translateTouchEvent(MotionEvent event)

// Get current pressure
public double getCurrentPressure()

// Set pressure tuning formula
public void setPressureTuneFormula(int formula)

// Save manual calibration values
public void saveManualReadings(float minValue, float maxValue)

// Launch calibration app
public void startCalibrateScreen()

// Pause audio
public boolean audioPause()

// Resume audio
public boolean audioResume()

// Set whether to use touch size
public void setUseTouchSize(boolean use)

// Change pressure smoothing option
public boolean ChangePressureSmoothOption(int option)

// Check if SonarPen is on screen
public boolean isSonarPenOnScreen()

// Check if USB-C connection
public boolean isSonarPenOnUSBC()

// Check if using manual calibration
public boolean isUsingManualCalibrate()

// Check if button is pressed
public boolean getSonicPenButtonPressed()
```

**Calibration Functions**:
```java
// Get current reading values
public void getCurrentReadingValue(SonarPenReadings readings)
public JSONObject getCurrReading()

// Save manual settings
public void saveManualReadings(float minValue, float maxValue)

// Manually set volume
public void setManualCalVol(int volume)
```

**Debug Functions**:
```java
public void setDebugLogOnOff(boolean enabled)
public boolean getDebugLogStatus()
```

### SonarPenCallBack Interface

```java
public interface SonarPenCallBack {
    // When SonarPen status changes
    void onSonarPenStatusChange(int status);
    
    // When SonarPen button is pressed
    void onSonarPenButtonPressed();
}
```

### SonarPenReadings Class

Data class storing pressure reading values:

```java
public class SonarPenReadings {
    public float minValue;           // Minimum amplitude value
    public float maxValue;           // Maximum amplitude value
    public float currentValue;       // Current amplitude value
    public float touchMinValue;      // Minimum value on touch
    public float pressure;           // Calculated pressure (0.0 ~ 1.0)
    public float manualMin;          // Manual calibration minimum
    public float manualMax;          // Manual calibration maximum
    public int currentSoundVol;      // Current volume
    public float currentFeq;         // Current frequency
    public int audioReadStatus;      // Audio read status
    public boolean bLowFreq;         // Whether in low frequency mode
    public String extraInfo;         // Additional information
}
```

### Usage Example

```java
// Initialize SonarPen
SonarPenUtilities sonarPen = new SonarPenUtilities(context);

// Set callback
sonarPen.addSonarPenCallback(new SonarPenCallBack() {
    @Override
    public void onSonarPenStatusChange(int status) {
        // Handle status change
    }
    
    @Override
    public void onSonarPenButtonPressed() {
        // Handle button press
    }
});

// Add to view
sonarPen.addSonarPenToView(drawView);

// Start
int result = sonarPen.start();
if (result == 0) {
    // Successfully started
}

// Touch event handling
@Override
public boolean onTouchEvent(MotionEvent event) {
    MotionEvent translatedEvent = sonarPen.translateTouchEvent(event);
    // Process translated event (pressure information added)
    return super.onTouchEvent(translatedEvent);
}

// On destroy
@Override
protected void onDestroy() {
    sonarPen.stop();
    super.onDestroy();
}
```

### Technical Details

**Audio Settings**:
- Sample Rate: 44100 Hz
- Channel: Mono (12)
- Encoding: PCM 16bit (2)
- Default Frequency: 9000 Hz
- Low Frequency Mode: 250 Hz

**Pressure Calculation Formulas**:
SonarPen supports multiple pressure tuning formulas:
```java
// Formula 0 (Default)
pressure = 2.21622 * x^3 - 1.91606 * x^2 + 0.7018 * x + 0.00008

// Formula 1
pressure = 0.7752 * x^3 - 0.30993 * x^2 + 0.53326 * x - 0.00056
```

**Calibration**:
- Auto Calibration: Automatically adjusts during use
- Manual Calibration: User sets minimum/maximum values
- File-Based: Saves settings to `/SonarPen/manual.setting`

**Permission Requirements**:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
```

---

## Summary

### API Feature Comparison Table

| Vendor | Motion Prediction | Pressure Detection | Tilt Detection | Button Events | Special Features |
|---------|------------------|-------------------|----------------|---------------|------------------|
| **OPPO/OnePlus** | ✅ Advanced | ✅ | ✅ | ✅ | Refresh rate optimization |
| **Samsung** | ❌ | ⚠️ S Pen | ⚠️ S Pen | ✅ | Air motion |
| **Huawei** | ✅ | ✅ | ✅ | ✅ | - |
| **Honor** | ✅ | ✅ | ✅ | ✅ | Color picker |
| **SonarPen** | ❌ | ✅ Acoustic | ❌ | ✅ | Universal device support |

### OPPO/OnePlus API Advantages

1. **Cutting-edge Motion Prediction**: Mature v2.0.0 algorithm
2. **Detailed Customization**: Fine-grained adjustment of DPI, refresh rate, and prediction time
3. **High Performance**: Fast processing via native C++ implementation
4. **Full Pressure & Tilt Support**: Optimal for professional drawing applications
5. **High-end Display Support**: Optimized for 120Hz+ high refresh rate displays

### Recommended Use Scenarios

**OPPO/OnePlus API**:
- Professional drawing and illustration apps
- Applications requiring low latency
- Apps leveraging high refresh rate displays

**Samsung S Pen API**:
- Galaxy device-specific apps
- Apps requiring remote operation (presentations, etc.)
- Apps utilizing air gestures

**Huawei/Honor API**:
- Apps requiring optimization for Huawei/Honor devices
- Apps utilizing color picking functionality (Honor only)

**SonarPen API**:
- Apps wanting to provide pressure detection across wide range of devices
- When wanting to achieve pressure without dedicated hardware
- Apps for users with budget constraints

### Integration Best Practices

```java
public class StylusManager {
    private MotionPredictor oppoPredictor;
    private SpenRemote samsungSpen;
    private SonarPenUtilities sonarPen;
    
    public void initialize(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        
        if (manufacturer.contains("oppo") || manufacturer.contains("oneplus")) {
            // Initialize OPPO/OnePlus API
            oppoPredictor = new MotionPredictor();
            oppoPredictor.setRefreshRate(getDisplayRefreshRate());
            oppoPredictor.setDpi(getDpiX(), getDpiY());
            
        } else if (manufacturer.contains("samsung")) {
            // Initialize Samsung API
            samsungSpen = SpenRemote.getInstance();
            samsungSpen.connect(context, connectionCallback);
            
        } else if (manufacturer.contains("huawei")) {
            // Initialize Huawei API
            if (HwPenEngineManager.isEngineRuntimeAvailable(context)) {
                HwStrokeEstimate.setRefreshRate(90.0f);
            }
            
        } else if (manufacturer.contains("honor")) {
            // Initialize Honor API
            if (HnPenEngineManager.isEngineRuntimeAvailable(context)) {
                HnStrokeEstimate.setRefreshRate(90.0f);
            }
            
        } else {
            // Fallback: SonarPen
            sonarPen = new SonarPenUtilities(context);
            sonarPen.start();
        }
    }
}
```

### Future Development Considerations

1. **API Version Management**: Handle version updates for each vendor API
2. **Performance Optimization**: Optimal parameter settings per device
3. **Fallback Strategy**: Alternative methods when API is unavailable
4. **User Settings**: UI for calibration and sensitivity adjustment
5. **Testing**: Thorough real device testing on each device

---

## References

### File Paths

- **OPPO/OnePlus**: `extract/src/sources/com/oplusos/vfxsdk/forecast/`
- **Samsung**: `extract/src/sources/com/samsung/android/sdk/penremote/`
- **Huawei**: `extract/src/sources/com/huawei/stylus/penengine/`
- **Honor**: `extract/src/sources/com/hihonor/android/magicx/app/penengine/`
- **SonarPen**: `extract/src/sources/com/greenbulb/sonarpen/`

### Related Classes

- **Main Integration**: `com/aige/hipaint/draw/ui/DrawMainUI.java` (26819 lines)
- **Native Surface**: `com/aige/hipaint/draw/opengl/NativeSurfaceView.java`
- **Pen Layer**: `com/aige/hipaint/draw/widget/PenLayerView.java`

---

**Document Version**: 1.0  
**Last Updated**: October 16, 2025

