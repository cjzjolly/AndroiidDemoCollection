package com.example.whiteboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapView mapView = new MapView(this);
        DrawView drawView = new DrawView(this);
        drawView.setMapView(mapView);
        drawView.setBitmapScale(MapView.MAX_SCALE);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("move and scale");
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    drawView.funChoice(DrawView.FuntionKind.MOVE_AND_SCALE);
                } else {
                    drawView.funChoice(DrawView.FuntionKind.DRAW);
                }
            }
        });
        linearLayout.addView(checkBox);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(mapView);
        frameLayout.addView(drawView);
        frameLayout.addView(linearLayout);

        setContentView(frameLayout);
    }
}
