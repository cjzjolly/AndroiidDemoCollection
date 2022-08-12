package com.example.eldestBmpOutCache;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class EldestBmpOutCache {
    private final int mMaxBytes = 20 * 1024 * 1024; //最多存储20MB数据
    private int mCurrentBytes = 0;
    private List<Item> mItemList = new ArrayList<>();
    private Object mReadWriteLock = new Object();

    public void add(Item item) {
        if (item == null || item.getBmp() == null || item.getBmp().isRecycled()) {
            return;
        }
        recycle(item.getByteCount()); //先检查有没有足够的控件，没有的话清理一部门老数据
        synchronized (mReadWriteLock) {
            mItemList.add(item);
            mCurrentBytes += item.getByteCount();
        }
        Log.i("cjztest", "添加條目:" + item);
    }

    public void remove(Item item) {
        if (item == null) {
            return;
        }
        synchronized (mReadWriteLock) {
            mItemList.remove(item);
            mCurrentBytes -= item.getByteCount();
        }
        Log.i("cjztest", "移除條目:" + item);
    }

    public Item getAtTag(int tag) {
        synchronized (mReadWriteLock) {
            for (Item item : mItemList) {
                if (item != null && item.getTag() == tag) {
                    return item;
                }
            }
        }
        return null;
    }

    public Item getAtPos(int pos) {
        synchronized (mReadWriteLock) {
            if (pos < mItemList.size()) {
                return mItemList.get(pos);
            }
        }
        return null;
    }

    public void recycle(int needSize) {
        //如果存储容量不够，清理最旧的图片：
        synchronized (mReadWriteLock) {
            if (mCurrentBytes + needSize > mMaxBytes) {
                long minTime = Long.MAX_VALUE;
                int minTimeItemIndex = -1;
                for (int i = 0; i < mItemList.size(); i++) {
                    Item item = mItemList.get(i);
                    if (item.getItemCreateTime() < minTime) {
                        minTime = item.getItemCreateTime();
                        minTimeItemIndex = i;
                    }
                }
                if (minTimeItemIndex != -1) {
                    Item deleted = mItemList.remove(minTimeItemIndex);
                    mCurrentBytes -= deleted.getByteCount();
                    Log.i("cjztest", "空間不足，清理條目:" + deleted);
                    if (mCurrentBytes + needSize > mMaxBytes) { //递归循环直到腾出足够空间
                        recycle(needSize);
                    }
                }
            }
        }
    }
}
