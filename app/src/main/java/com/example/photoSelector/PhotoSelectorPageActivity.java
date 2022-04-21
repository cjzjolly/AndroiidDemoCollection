package com.example.photoSelector;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.picCutview.PicCutView;
import com.example.piccut.R;

public class PhotoSelectorPageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new PhotoSelectorView(this));
    }
}