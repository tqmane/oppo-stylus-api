package com.example.stylus.noteapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        drawingView = findViewById(R.id.drawing_view);
        
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            drawingView.clear();
            Toast.makeText(this, "Canvas cleared", Toast.LENGTH_SHORT).show();
        });
        
        // Show prediction status
        String status = drawingView.isPredictionEnabled() ? 
            "OPPO Prediction: ON" : "Standard Touch";
        Toast.makeText(this, status, Toast.LENGTH_LONG).show();
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

