package com.example.stylus.noteapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.Arrays;
import com.oplusos.vfxsdk.forecast.MotionPredictor;
import com.oplusos.vfxsdk.forecast.TouchPointInfo;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private List<Stroke> strokes = new ArrayList<>();
    private Stroke currentStroke;
    private Paint paint;
    private MotionPredictor predictor;
    private int currentColor = Color.BLACK;
    private float strokeWidth = 5f;
    private boolean usePrediction = false;
    
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(strokeWidth);
        
        // OPPO/OnePlus predictor initialization (be defensive against native init errors)
        try {
            if (!isArm64Device()) {
                // Emulator/x86/x64等ではネイティブが存在しないためスキップ
                usePrediction = false;
                return;
            }
            predictor = new MotionPredictor();
            DisplayMetrics metrics = getDisplayMetrics(context);
            float refreshRate = getRefreshRate(context);
            predictor.setDpi(metrics.xdpi, metrics.ydpi);
            predictor.setRefreshRate(refreshRate);
            predictor.setMaxPredictTime(16.0f);
            usePrediction = true;
        } catch (Throwable e) {
            // Not OPPO/OnePlus device or native init failed; fall back to standard touch
            usePrediction = false;
        }
    }

    private boolean isArm64Device() {
        String[] abis = Build.SUPPORTED_ABIS;
        if (abis == null) return false;
        for (String abi : abis) {
            if ("arm64-v8a".equalsIgnoreCase(abi)) {
                return true;
            }
        }
        return false;
    }
    
    private DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics;
    }
    
    private float getRefreshRate(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            return display.getRefreshRate();
        }
        return 60.0f;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float pressure = event.getPressure();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (usePrediction && predictor != null) {
                    predictor.reset();
                }
                currentStroke = new Stroke(currentColor);
                currentStroke.addPoint(x, y, pressure);
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (currentStroke != null) {
                    if (usePrediction && predictor != null) {
                        TouchPointInfo point = new TouchPointInfo(x, y, pressure, 0f, event.getEventTime());
                        predictor.pushTouchPoint(point);
                        TouchPointInfo predicted = predictor.predictTouchPoint();
                        if (predicted != null) {
                            currentStroke.addPoint(predicted.x, predicted.y, predicted.pressure);
                        } else {
                            currentStroke.addPoint(x, y, pressure);
                        }
                    } else {
                        currentStroke.addPoint(x, y, pressure);
                    }
                }
                break;
                
            case MotionEvent.ACTION_UP:
                if (currentStroke != null) {
                    currentStroke.addPoint(x, y, pressure);
                    strokes.add(currentStroke);
                    currentStroke = null;
                }
                break;
        }
        
        invalidate();
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (Stroke stroke : strokes) {
            stroke.draw(canvas, paint, strokeWidth);
        }
        
        if (currentStroke != null) {
            currentStroke.draw(canvas, paint, strokeWidth);
        }
    }
    
    public void clear() {
        strokes.clear();
        currentStroke = null;
        invalidate();
    }
    
    public void undo() {
        if (!strokes.isEmpty()) {
            strokes.remove(strokes.size() - 1);
            invalidate();
        }
    }
    
    public void setColor(int color) {
        this.currentColor = color;
    }
    
    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
    }
    
    public boolean isPredictionEnabled() {
        return usePrediction;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (predictor != null) {
            predictor.destroy();
        }
    }
    
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
        
        public void draw(Canvas canvas, Paint paint, float baseWidth) {
            int oldColor = paint.getColor();
            float oldWidth = paint.getStrokeWidth();
            
            paint.setColor(color);
            
            if (!points.isEmpty()) {
                for (int i = 1; i < points.size(); i++) {
                    PointData prev = points.get(i - 1);
                    PointData curr = points.get(i);
                    
                    float avgPressure = (prev.pressure + curr.pressure) / 2f;
                    paint.setStrokeWidth(baseWidth * Math.max(0.3f, avgPressure));
                    
                    canvas.drawLine(prev.x, prev.y, curr.x, curr.y, paint);
                }
            }
            
            paint.setColor(oldColor);
            paint.setStrokeWidth(oldWidth);
        }
    }
    
    private static class PointData {
        float x, y, pressure;
        
        PointData(float x, float y, float pressure) {
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }
    }
}

