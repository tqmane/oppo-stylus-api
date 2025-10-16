package com.oplusos.vfxsdk.forecast;

import android.util.Log;

public class MotionPredictor {
    public static final String TAG = "MotionPredictor";
    public long mNativeHandle = 0;
    public long mPreviousTime = 0;
    
    public MotionPredictor() {
        create();
    }
    
    public final void create() {
        this.mNativeHandle = NativeForecast.create(this.mNativeHandle);
    }
    
    public void setRefreshRate(float refreshRate) {
        NativeForecast.setRefreshRate(this.mNativeHandle, refreshRate);
    }
    
    public void setDpi(float dpiX, float dpiY) {
        NativeForecast.setDpi(this.mNativeHandle, dpiX, dpiY);
    }
    
    public void setMaxPredictTime(float maxPredictTime) {
        NativeForecast.setMaxPredictTime(this.mNativeHandle, maxPredictTime);
    }
    
    public void reset() {
        this.mPreviousTime = System.nanoTime() / 1000000;
        NativeForecast.reset(this.mNativeHandle);
    }
    
    public void pushTouchPoint(TouchPointInfo touchPointInfo) {
        touchPointInfo.timestamp = touchPointInfo.timestamp - this.mPreviousTime;
        NativeForecast.pushTouchPoint(this.mNativeHandle, touchPointInfo);
    }
    
    public TouchPointInfo predictTouchPoint() {
        TouchPointInfo predictTouchPoint = NativeForecast.predictTouchPoint(this.mNativeHandle);
        if (predictTouchPoint != null) {
            predictTouchPoint.timestamp += this.mPreviousTime;
        }
        return predictTouchPoint;
    }
    
    public void destroy() {
        NativeForecast.destroy(this.mNativeHandle);
    }
}

