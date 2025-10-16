package com.oplusos.vfxsdk.forecast;

public class TouchPointInfo {
    public float x;
    public float y;
    public float pressure;
    public float axisTilt;
    public long timestamp;
    
    public TouchPointInfo(float x, float y, float pressure, float axisTilt, long timestamp) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.axisTilt = axisTilt;
        this.timestamp = timestamp;
    }
    
    public TouchPointInfo() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.pressure = 0.0f;
        this.axisTilt = 0.0f;
        this.timestamp = 0L;
    }
}

