package com.example.stylus.noteapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: setContentView done");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Log.d(TAG, "onCreate: toolbar set");
        } else {
            Log.w(TAG, "onCreate: toolbar is null");
        }
        
        drawingView = findViewById(R.id.drawing_view);
        if (drawingView == null) {
            Log.e(TAG, "onCreate: drawingView is null");
        } else {
            Log.d(TAG, "onCreate: drawingView found, prediction=" + drawingView.isPredictionEnabled());
        }
        
        FloatingActionButton fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(view -> {
                if (drawingView != null) {
                    drawingView.clear();
                    Toast.makeText(this, "Canvas cleared", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w(TAG, "onCreate: fab is null");
        }
        
        // Show prediction status
        String status = (drawingView != null && drawingView.isPredictionEnabled()) ?
            "OPPO Prediction: ON" : "Standard Touch";
        Toast.makeText(this, status, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onCreate: end, status=" + status);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_undo) {
            drawingView.undo();
            return true;
        } else if (id == R.id.action_black) {
            drawingView.setColor(Color.BLACK);
            return true;
        } else if (id == R.id.action_red) {
            drawingView.setColor(Color.RED);
            return true;
        } else if (id == R.id.action_blue) {
            drawingView.setColor(Color.BLUE);
            return true;
        } else if (id == R.id.action_green) {
            drawingView.setColor(Color.GREEN);
            return true;
        } else if (id == R.id.action_thin) {
            drawingView.setStrokeWidth(3f);
            return true;
        } else if (id == R.id.action_medium) {
            drawingView.setStrokeWidth(7f);
            return true;
        } else if (id == R.id.action_thick) {
            drawingView.setStrokeWidth(12f);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}

