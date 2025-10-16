package com.oplusos.vfxsdk.forecast;

import android.util.Log;

/**
 * OPPO/OnePlus モーション予測クラス
 * 
 * タッチイベントの軌跡を予測し、低遅延描画を実現します。
 * 
 * 使用例:
 * <pre>
 * MotionPredictor predictor = new MotionPredictor();
 * predictor.setRefreshRate(120.0f);
 * predictor.setDpi(440.0f, 440.0f);
 * predictor.setMaxPredictTime(16.0f);
 * predictor.reset();
 * 
 * // タッチイベント処理
 * TouchPointInfo current = new TouchPointInfo(x, y, pressure, tilt, timestamp);
 * predictor.pushTouchPoint(current);
 * TouchPointInfo predicted = predictor.predictTouchPoint();
 * </pre>
 */
public class MotionPredictor {
    public static final String TAG = "Forecast:MotionPredictor";
    public static final String VERSION = "VERSION_2.0.0";
    
    public long mNativeHandle = 0;
    public long mPreviousTime = 0;
    
    /**
     * コンストラクタ
     */
    public MotionPredictor() {
        Log.d(TAG, "motion predictor version: " + VERSION);
        create();
    }
    
    /**
     * ネイティブハンドルを作成
     */
    public final void create() {
        this.mNativeHandle = NativeForecast.create(this.mNativeHandle);
    }
    
    /**
     * リフレッシュレートを設定
     * @param refreshRate リフレッシュレート（Hz）例: 60.0, 90.0, 120.0
     */
    public void setRefreshRate(float refreshRate) {
        NativeForecast.setRefreshRate(this.mNativeHandle, refreshRate);
    }
    
    /**
     * 画面のDPIを設定
     * @param dpiX X軸のDPI
     * @param dpiY Y軸のDPI
     */
    public void setDpi(float dpiX, float dpiY) {
        NativeForecast.setDpi(this.mNativeHandle, dpiX, dpiY);
    }
    
    /**
     * 最大予測時間を設定
     * @param maxPredictTime 最大予測時間（ミリ秒）例: 16.0
     */
    public void setMaxPredictTime(float maxPredictTime) {
        NativeForecast.setMaxPredictTime(this.mNativeHandle, maxPredictTime);
    }
    
    /**
     * 予測システムをリセット
     * 新しいストローク開始時などに呼び出してください
     */
    public void reset() {
        this.mPreviousTime = System.nanoTime() / 1000000;
        NativeForecast.reset(this.mNativeHandle);
    }
    
    /**
     * タッチポイントをキューに追加
     * @param touchPointInfo タッチポイント情報
     */
    public void pushTouchPoint(TouchPointInfo touchPointInfo) {
        Log.d(TAG, "push point: x " + touchPointInfo.x + ", y " + touchPointInfo.y + 
              ", t " + touchPointInfo.timestamp);
        touchPointInfo.timestamp = touchPointInfo.timestamp - this.mPreviousTime;
        NativeForecast.pushTouchPoint(this.mNativeHandle, touchPointInfo);
    }
    
    /**
     * 次のタッチポイントを予測
     * @return 予測されたタッチポイント（null の場合は予測なし）
     */
    public TouchPointInfo predictTouchPoint() {
        TouchPointInfo predictTouchPoint = NativeForecast.predictTouchPoint(this.mNativeHandle);
        if (predictTouchPoint != null) {
            predictTouchPoint.timestamp += this.mPreviousTime;
            Log.d(TAG, "predict point: x " + predictTouchPoint.x + ", y " + predictTouchPoint.y + 
                  ", t " + predictTouchPoint.timestamp);
        }
        return predictTouchPoint;
    }
    
    /**
     * リソースを解放
     * アクティビティのonDestroy()などで呼び出してください
     */
    public void destroy() {
        NativeForecast.destroy(this.mNativeHandle);
    }
}

