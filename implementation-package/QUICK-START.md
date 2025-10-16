# ⚡ クイックスタート - 5分で動かす

最速で動作するサンプルを作成する手順

## 📝 前提条件

- Android Studio インストール済み
- 新規プロジェクト作成済み
- OPPO/OnePlusデバイス（実機またはエミュレータ）

## 🚀 3ステップで完了

### Step 1: ファイルをコピー (2分)

```bash
# 1. ネイティブライブラリ
implementation-package/libs/oppo-forecast/arm64-v8a/libforecast.so
  → your-project/app/src/main/jniLibs/arm64-v8a/libforecast.so

# 2. Javaファイル（3つ）
implementation-package/java/interfaces/*.java
  → your-project/app/src/main/java/com/oplusos/vfxsdk/forecast/
```

### Step 2: コードを書く (2分)

#### MainActivity.java

```java
package com.example.myapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.oplusos.vfxsdk.forecast.MotionPredictor;
import com.oplusos.vfxsdk.forecast.TouchPointInfo;

public class MainActivity extends AppCompatActivity {
    private MotionPredictor predictor;
    private TextView textView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // シンプルなレイアウト
        textView = new TextView(this);
        textView.setText("Touch the screen!");
        textView.setTextSize(24);
        setContentView(textView);
        
        // 初期化
        predictor = new MotionPredictor();
        predictor.setRefreshRate(120.0f);
        predictor.setDpi(440.0f, 440.0f);
        predictor.setMaxPredictTime(16.0f);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            predictor.reset();
        }
        
        // タッチポイントを追加
        TouchPointInfo point = new TouchPointInfo(
            event.getX(),
            event.getY(),
            event.getPressure(),
            0f,
            event.getEventTime()
        );
        predictor.pushTouchPoint(point);
        
        // 予測を取得
        TouchPointInfo predicted = predictor.predictTouchPoint();
        if (predicted != null) {
            textView.setText(String.format(
                "Current: (%.0f, %.0f)\nPredicted: (%.0f, %.0f)\nPressure: %.2f",
                point.x, point.y,
                predicted.x, predicted.y,
                predicted.pressure
            ));
        }
        
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (predictor != null) {
            predictor.destroy();
        }
    }
}
```

### Step 3: ビルド＆実行 (1分)

1. **Sync Gradle**
2. **Run**
3. **画面をタッチ** → 予測座標が表示される！

## ✅ 動作確認

成功すると以下が表示されます：
```
Current: (123, 456)
Predicted: (127, 460)
Pressure: 0.85
```

## 🎨 次のステップ

動いたら、以下を試してください：

### 描画機能を追加

```java
public class DrawView extends View {
    private MotionPredictor predictor;
    private Path path = new Path();
    private Paint paint = new Paint();
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TouchPointInfo predicted = predictor.processTouchEvent(event);
        if (predicted != null) {
            path.lineTo(predicted.x, predicted.y);
            invalidate();
        }
        return true;
    }
}
```

### 筆圧対応

```java
// 筆圧に応じて線の太さを変える
float width = predicted.pressure * 20f;
paint.setStrokeWidth(width);
```

### マルチデバイス対応

実装ガイドの `UnifiedStylusManager` を使用

## ❗ トラブルシューティング

### エラー: UnsatisfiedLinkError

**原因**: ライブラリが見つからない

**解決策**:
```
1. jniLibs フォルダ構造を確認
   app/src/main/jniLibs/arm64-v8a/libforecast.so
2. Clean Project → Rebuild
```

### エラー: ClassNotFoundException

**原因**: パッケージ名が間違っている

**解決策**:
```
パッケージ名を確認:
com.oplusos.vfxsdk.forecast.TouchPointInfo
com.oplusos.vfxsdk.forecast.NativeForecast  
com.oplusos.vfxsdk.forecast.MotionPredictor
```

### 予測が null

**原因**: reset() を呼び忘れ

**解決策**:
```java
if (event.getAction() == MotionEvent.ACTION_DOWN) {
    predictor.reset();  // これを追加！
}
```

## 📚 より詳しく

- [SETUP.md](SETUP.md) - 詳細なセットアップ
- [実装ガイド](../docs/implementation-guide.md) - 完全な実装
- [統合チェックリスト](INTEGRATION-CHECKLIST.md) - 確認項目

---

**所要時間**: 約5分  
**難易度**: ⭐☆☆☆☆ (初心者OK)

🎉 お疲れ様でした！OPPO/OnePlusのスタイラスAPIが動きました！

