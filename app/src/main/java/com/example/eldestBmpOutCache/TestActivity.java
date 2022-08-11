package com.example.eldestBmpOutCache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TestActivity extends Activity {

    private EldestBmpOutCache mEldestBmpOutCache = new EldestBmpOutCache();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //進行添加測試:
        for (int i = 0; i < 200; i++) {
            Bitmap bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
            Item item = new Item(bitmap, i);
            mEldestBmpOutCache.add(item);
        }
    }
}
