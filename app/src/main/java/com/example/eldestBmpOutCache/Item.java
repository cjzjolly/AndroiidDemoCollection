package com.example.eldestBmpOutCache;

import android.graphics.Bitmap;

public class Item {
    private long mItemCreateTime = 0;
    private Bitmap mBmp;
    private int mTag;
    private int mByteCount = 0;

    public long getItemCreateTime() {
        return mItemCreateTime;
    }

    public Item(Bitmap bitmap, int tag) {
        mTag = tag;
        mByteCount = bitmap.getByteCount();
        mBmp = bitmap;
        mItemCreateTime = System.nanoTime();
    }

    public Bitmap getBmp() {
        return mBmp;
    }

    public int getTag() {
        return mTag;
    }

    public int getByteCount() {
        return mByteCount;
    }
}
