package com.oplusos.vfxsdk.forecast;

/**
 * OPPO/OnePlus ネイティブ予測ライブラリへのJNIインターフェース
 * 
 * このクラスはOPPO/OnePlusのネイティブライブラリ(libforecast.so)と通信します。
 * パッケージ名は変更しないでください。
 */
class NativeForecast {
    /**
     * ネイティブハンドルを作成
     * @param handle 既存のハンドル（初回は0）
     * @return 新しいネイティブハンドル
     */
    public static native long create(long handle);
    
    /**
     * 予測エンジンを破棄
     * @param handle ネイティブハンドル
     */
    public static native void destroy(long handle);
    
    /**
     * 予測タッチポイントを取得
     * @param handle ネイティブハンドル
     * @return 予測されたタッチポイント
     */
    public static native TouchPointInfo predictTouchPoint(long handle);
    
    /**
     * タッチポイントをプッシュ
     * @param handle ネイティブハンドル
     * @param touchPointInfo タッチポイント情報
     */
    public static native void pushTouchPoint(long handle, TouchPointInfo touchPointInfo);
    
    /**
     * システムをリセット
     * @param handle ネイティブハンドル
     */
    public static native void reset(long handle);
    
    /**
     * リフレッシュレートを設定
     * @param handle ネイティブハンドル
     * @param refreshRate リフレッシュレート（Hz）
     */
    public static native void setRefreshRate(long handle, float refreshRate);
    
    /**
     * DPIを設定
     * @param handle ネイティブハンドル
     * @param dpiX X軸のDPI
     * @param dpiY Y軸のDPI
     */
    public static native void setDpi(long handle, float dpiX, float dpiY);
    
    /**
     * 最大予測時間を設定
     * @param handle ネイティブハンドル
     * @param maxPredictTime 最大予測時間（ミリ秒）
     */
    public static native void setMaxPredictTime(long handle, float maxPredictTime);
    
    /**
     * ネイティブライブラリをロード
     */
    static {
        System.loadLibrary("forecast");
    }
}

