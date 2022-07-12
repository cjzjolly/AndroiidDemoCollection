package com.example.whiteboard;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**图块管理系统**/
public class MapImageManager {
    private static File mRootDir = null;
    /**todo 加一块限制容量的内存块，在容量允许的情况下，最近读写过的压缩文件数据可以放进来**/


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
//        Log.i("cjztest", "tileFile:" + tileFile.getAbsolutePath());
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
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            int w = tileBmp.getWidth();
            int h = tileBmp.getHeight();
            gzipOutputStream.write((byte) ((w >> 24) & 0xFF)); //写入图块w
            gzipOutputStream.write((byte) ((w >> 16) & 0xFF)); //写入图块w
            gzipOutputStream.write((byte) ((w >> 8) & 0xFF)); //写入图块w
            gzipOutputStream.write((byte) (w & 0xFF)); //写入图块w
            gzipOutputStream.write((byte) ((h >> 24) & 0xFF)); //写入图块h
            gzipOutputStream.write((byte) ((h >> 16) & 0xFF)); //写入图块h
            gzipOutputStream.write((byte) ((h >> 8) & 0xFF)); //写入图块h
            gzipOutputStream.write((byte) (h & 0xFF)); //写入图块h
            byte pixels[] = new byte[tileBmp.getWidth() * tileBmp.getHeight() * 4];
            tileBmp.copyPixelsToBuffer(ByteBuffer.wrap(pixels));
            gzipOutputStream.write(pixels);
            gzipOutputStream.flush();
//            fileOutputStream.close();
            gzipOutputStream.close();
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
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
            byte wByteArray[] = new byte[4];
            byte hByteArray[] = new byte[4];
            gzipInputStream.read(wByteArray);
            gzipInputStream.read(hByteArray);
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
            byte mReadPixelsBuf[] = new byte[width * height * 4];
            int status = 0;
            int pos = 0;
            int lenStep = 4096 * 8;
            while ((status = gzipInputStream.read(mReadPixelsBuf, pos, lenStep)) != -1) {
                pos += status;
                if (pos + lenStep > mReadPixelsBuf.length) {
                    lenStep = mReadPixelsBuf.length - pos;
                    if (lenStep <= 0) {
                        break;
                    }
                }
                //方法2 但这样数据不完整
//                if (pos + 4096 >= mReadPixelsBuf.length) {
//                    break;
//                }
            }
//            gzipInputStream.read(mReadPixelsBuf);  // 这个东西不能一口气读写太多数据
            unitPixelBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mReadPixelsBuf));
            fileInputStream.close();
            gzipInputStream.close();
//            Log.i("cjztest", String.format("read:[%d, %d]", tag[0], tag[1]));
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
