package com.oplusos.vfxsdk.forecast;

/**
 * OPPO/OnePlus タッチポイント情報
 * 
 * このクラスはOPPO/OnePlusのモーション予測APIで使用されます。
 * あなたのプロジェクトにコピーして使用してください。
 * 
 * パッケージ名は変更しないでください（ネイティブライブラリとの互換性のため）
 */
public class TouchPointInfo {
    public float x;           // X座標
    public float y;           // Y座標  
    public float pressure;    // 筆圧 (0.0 ~ 1.0)
    public float axisTilt;    // ペンの傾き角度
    public long timestamp;    // タイムスタンプ（ミリ秒）
    
    /**
     * パラメータ付きコンストラクタ
     */
    public TouchPointInfo(float x, float y, float pressure, float axisTilt, long timestamp) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.axisTilt = axisTilt;
        this.timestamp = timestamp;
    }
    
    /**
     * デフォルトコンストラクタ（すべて0で初期化）
     */
    public TouchPointInfo() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.pressure = 0.0f;
        this.axisTilt = 0.0f;
        this.timestamp = 0L;
    }
}

