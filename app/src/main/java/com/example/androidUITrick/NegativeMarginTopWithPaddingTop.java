package com.example.androidUITrick;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.piccut.R;

/**cjzmark todo 展示使用一种使用负值marginTop搭配正值paddingTop的控件编排方式**/
public class NegativeMarginTopWithPaddingTop extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negative_margin_top_padding_top);
    }
}
