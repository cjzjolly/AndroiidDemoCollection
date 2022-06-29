package com.example.whiteboard;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

/**图块管理系统**/
public class MapImageManager {
    /**todo 暂时用map模拟外存**/
    private static Map<Integer, Map<Integer, Bitmap>> mBmpMap = new HashMap<>();


    public static void saveTileImage(int tag[], Bitmap tileBmp, float currentScale) {
        if (null == mBmpMap.get(tag[0])) {
            mBmpMap.put(tag[0], new HashMap<>());
        }
        if (null == mBmpMap.get(tag[0]).get(tag[1])) {
            mBmpMap.get(tag[0]).put(tag[1], tileBmp);
        }
    }

    public static Bitmap getTileImage(int tag[], float currentScale) {
        if (null == mBmpMap.get(tag[0])) {
            return null;
        }
        if (null == mBmpMap.get(tag[0]).get(tag[1])) {
            return null;
        }
        return mBmpMap.get(tag[0]).get(tag[1]);
    }
}
