package com.oplusos.vfxsdk.forecast;

class NativeForecast {
    public static native long create(long handle);
    public static native void destroy(long handle);
    public static native TouchPointInfo predictTouchPoint(long handle);
    public static native void pushTouchPoint(long handle, TouchPointInfo info);
    public static native void reset(long handle);
    public static native void setRefreshRate(long handle, float rate);
    public static native void setDpi(long handle, float dpiX, float dpiY);
    public static native void setMaxPredictTime(long handle, float maxTime);
    
    static {
        System.loadLibrary("forecast");
    }
}

