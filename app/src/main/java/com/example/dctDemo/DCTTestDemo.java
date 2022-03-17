package com.example.dctDemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DCTTestDemo extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout totalView = new LinearLayout(this);
        totalView.setOrientation(LinearLayout.VERTICAL);
        totalView.setGravity(Gravity.CENTER);
        for (int y = 0; y < 8; y ++) {
            LinearLayout hLinearLayout = new LinearLayout(this);
            hLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < 8; x ++) {
                DCTBaseView b = new DCTBaseView(this);
                b.calcDCTBase(x, y);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(8 * 15, 8 * 15);
                layoutParams.setMargins(8, 8, 8, 8);
                b.setLayoutParams(layoutParams);
                hLinearLayout.addView(b);
            }
            totalView.addView(hLinearLayout);
        }
        setContentView(totalView);
    }
}
