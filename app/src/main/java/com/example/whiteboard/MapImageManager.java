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
    private static File mRootDir = null;


    public static void saveTileImage(int tag[], Bitmap tileBmp, float currentScale) {
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
        Log.i("cjztest", "tileFile:" + tileFile.getAbsolutePath());
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
            int w = tileBmp.getWidth();
            int h = tileBmp.getHeight();
            fileOutputStream.write((byte) ((w >> 24) & 0xFF)); //写入图块w
            fileOutputStream.write((byte) ((w >> 16) & 0xFF)); //写入图块w
            fileOutputStream.write((byte) ((w >> 8) & 0xFF)); //写入图块w
            fileOutputStream.write((byte) (w & 0xFF)); //写入图块w
            fileOutputStream.write((byte) ((h >> 24) & 0xFF)); //写入图块h
            fileOutputStream.write((byte) ((h >> 16) & 0xFF)); //写入图块h
            fileOutputStream.write((byte) ((h >> 8) & 0xFF)); //写入图块h
            fileOutputStream.write((byte) (h & 0xFF)); //写入图块h
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
            byte wByteArray[] = new byte[4];
            byte hByteArray[] = new byte[4];
            fileInputStream.read(wByteArray);
            fileInputStream.read(hByteArray);
            int width = 0;
            int height = 0;
            width |= ((wByteArray[0] & 0xFF) << 24);
            width |= ((wByteArray[1] & 0xFF) << 16);
            width |= ((wByteArray[2] & 0xFF) << 8);
            width |= (wByteArray[3] & 0xFF);
            height |= ((hByteArray[0] & 0xFF) << 24);
            height |= ((hByteArray[1] & 0xFF) << 16);
            height |= ((hByteArray[2] & 0xFF) << 8);
            height |= (hByteArray[3] & 0xFF);
            Bitmap unitPixelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            readPixels = new byte[width * height * 4];
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
