package com.example.whiteboard;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**图块管理系统**/
public class MapImageManager {
    /**todo 暂时用map模拟外存**/
    private static Map<Integer, Map<Integer, Bitmap>> mBmpMap = new HashMap<>();
    private static File mRootDir = null;


    public static void saveTileImage(int tag[], Bitmap tileBmp, float currentScale) {
//        if (null == mBmpMap.get(tag[0])) {
//            mBmpMap.put(tag[0], new HashMap<>());
//        }
//        if (null == mBmpMap.get(tag[0]).get(tag[1])) {
//            mBmpMap.get(tag[0]).put(tag[1], tileBmp);
//        }




        if (null == mRootDir) {
            return;
        }
        File levelFirst = new File(mRootDir.getAbsolutePath(), String.valueOf(tag[0]));
        if (!levelFirst.exists()) {
            levelFirst.mkdir();
        }
        File levelSec = new File(levelFirst.getAbsolutePath(), String.valueOf(tag[1]));
        if (!levelSec.exists()) {
            levelSec.mkdir();
        }
        File tileFile = new File(levelSec, "tile.raw");
        if (!tileFile.exists()) {
            try {
                tileFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            tileFile.delete();
            try {
                tileFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(tileFile);
            fileOutputStream.write((byte) (tileBmp.getWidth() & 0xFF)); //写入图块边长
            byte pixels[] = new byte[tileBmp.getWidth() * tileBmp.getHeight() * 4];
            tileBmp.copyPixelsToBuffer(ByteBuffer.wrap(pixels));
            fileOutputStream.write(pixels);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte readPixels[] = null;
    public static Bitmap getTileImage(int tag[], float currentScale) {
//        if (null == mBmpMap.get(tag[0])) {
//            return null;
//        }
//        if (null == mBmpMap.get(tag[0]).get(tag[1])) {
//            return null;
//        }
//        return mBmpMap.get(tag[0]).get(tag[1]);


        if (null == mRootDir) {
            return null;
        }
        File levelFirst = new File(mRootDir.getAbsolutePath(), String.valueOf(tag[0]));
        if (!levelFirst.exists()) {
            return null;
        }
        File levelSec = new File(levelFirst.getAbsolutePath(), String.valueOf(tag[1]));
        if (!levelSec.exists()) {
            return null;
        }
        File tileFile = new File(levelSec, "tile.raw");
        if (!tileFile.exists()) {
            return null;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(tileFile);
            int width = fileInputStream.read() & 0xFF;
            Log.e("cjztest", "边张:" + width);
            Bitmap unitPixelBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            readPixels = new byte[width * width * 4];
            fileInputStream.read(readPixels);
            unitPixelBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(readPixels));
            fileInputStream.close();
            return unitPixelBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setRootPath(File rootPath) {
        mRootDir = rootPath;
    }
}
