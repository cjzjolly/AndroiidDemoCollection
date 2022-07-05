package com.example.whiteboard;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**图块管理系统**/
public class MapImageManager {
    private static File mRootDir = null;
    private static byte mReadPixelsBuf[] = null;


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
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            zipOutputStream.putNextEntry(new ZipEntry("content"));
            int w = tileBmp.getWidth();
            int h = tileBmp.getHeight();
            zipOutputStream.write((byte) ((w >> 24) & 0xFF)); //写入图块w
            zipOutputStream.write((byte) ((w >> 16) & 0xFF)); //写入图块w
            zipOutputStream.write((byte) ((w >> 8) & 0xFF)); //写入图块w
            zipOutputStream.write((byte) (w & 0xFF)); //写入图块w
            zipOutputStream.write((byte) ((h >> 24) & 0xFF)); //写入图块h
            zipOutputStream.write((byte) ((h >> 16) & 0xFF)); //写入图块h
            zipOutputStream.write((byte) ((h >> 8) & 0xFF)); //写入图块h
            zipOutputStream.write((byte) (h & 0xFF)); //写入图块h
            byte pixels[] = new byte[tileBmp.getWidth() * tileBmp.getHeight() * 4];
            tileBmp.copyPixelsToBuffer(ByteBuffer.wrap(pixels));
            zipOutputStream.write(pixels);
            zipOutputStream.setComment(tileFile.getName());


//            fileOutputStream.close();
            zipOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            if (zipEntry == null) {
                return null;
            }
            byte wByteArray[] = new byte[4];
            byte hByteArray[] = new byte[4];
            zipInputStream.read(wByteArray);
            zipInputStream.read(hByteArray);
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
            Log.i("cjztest", String.format("wh:[%d, %d]", width, height));

            Bitmap unitPixelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mReadPixelsBuf = new byte[width * height * 4];
            zipInputStream.read(mReadPixelsBuf);
            unitPixelBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mReadPixelsBuf));
            Log.i("cjztest", String.format("read:[%d, %d]", tag[0], tag[1]));
            zipInputStream.closeEntry();
            zipInputStream.close();
//            fileInputStream.close();
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
