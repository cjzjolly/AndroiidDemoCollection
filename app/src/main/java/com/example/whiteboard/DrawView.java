package com.example.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cjz on 2019/11/25.
 * 绘图用的窗体
 */

public class DrawView extends View {
    private Curv mCurrentCurv;

    /**当前绘制画布**/
    private Canvas mCanvas;

    /**瓦片载体高清画布**/
    private Canvas mCanvasScale;

    /**进行过初始化了吗**/
    private boolean isInitFinished = false;

    /**数据读写根目录**/
    private String rootPath;

    /**控件长宽**/
    private int mWidth, mHeight;

    /**当前绘制画布**/
    private Bitmap mCanvasBitmap;

    /**瓦片载体高清画布**/
    private Bitmap mCanvasScaleBitmap;

    /**是否绘制触摸**/
    private boolean isShowTouchEvent = true;

    /**事件累积**/
    private StringBuffer touchEventStringBuffer = new StringBuffer();



    /**当前正在绘制的线条组合**/
    private Map<Integer, Curv> currentDrawingMap = new HashMap<>();

    /**位图放大倍数**/
    private float mMaxScale = 1f;

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**设置画布所依赖的位图放大的倍数，使用瓦片载体的最大缩放倍数即可，这样瓦片位图的碎片就是从最高分辨率的图中获取的，这样缩小还是放大都不会模糊了**/
    public void setBitmapScale(float maxScale) {
        this.mMaxScale = maxScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isInitFinished) {
            rootPath = getContext().getFilesDir().getAbsolutePath() + File.separatorChar + "drawView";
            File rootDir = new File(rootPath);
            if(!rootDir.exists()){
                rootDir.mkdir();
            }
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (width == 0 || height == 0) {
                return;
            }
            mWidth = width;
            mHeight = height;
            mCanvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mCanvasBitmap);
            mCanvasScaleBitmap = Bitmap.createBitmap((int) (mWidth * mMaxScale), (int) (mHeight * mMaxScale), Bitmap.Config.ARGB_8888);
            mCanvasScale = new Canvas(mCanvasScaleBitmap);
            isInitFinished = true;
        }
    }


    /**获取绘制笔**/
    private Paint makePaint() {
        Paint paint = new Paint();
        paint.setStrokeWidth(12f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        int color = 0xFF000000;
        //随机颜色
        color |= ((int) (Math.random() * 255 + 1) << 16);
        color |= ((int) (Math.random() * 255 + 1) << 8);
        color |= ((int) (Math.random() * 255 + 1));
        paint.setColor(color);
        return paint;
    }

    /**书写**/
    private void penDraw(MotionEvent event) {
        int actionType = event.getAction() & MotionEvent.ACTION_MASK;

        switch (actionType){
            case MotionEvent.ACTION_POINTER_DOWN: {
                Log.i("penDraw_AT", "MotionEvent.ACTION_POINTER_DOWN");
                int id = event.getPointerId(event.getActionIndex());
                touchEventStringBuffer.append("MotionEvent.ACTION_DOWN, id:" + id + "\n");
                Paint paint = makePaint();
                mCurrentCurv = new Curv(paint);
                mCurrentCurv.draw(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()), event.getAction(), mCanvas);
                currentDrawingMap.put(id, mCurrentCurv);
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                int id = event.getPointerId(event.getActionIndex());
                touchEventStringBuffer.append("MotionEvent.ACTION_DOWN, id:" + id + "\n");
                Paint paint = makePaint();
                mCurrentCurv = new Curv(paint);
                mCurrentCurv.draw(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()), event.getAction(), mCanvas);
                currentDrawingMap.put(id, mCurrentCurv);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    touchEventStringBuffer.append("MotionEvent.ACTION_MOVE, id:" + id + "\n");
                    Curv curv = (Curv) currentDrawingMap.get(id);
                    if (curv != null) {
                        curv.draw(event.getX(i), event.getY(i), event.getAction(), mCanvas);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                int id = event.getPointerId(event.getActionIndex());


                Curv curv = (Curv) currentDrawingMap.get(id);
                if (curv != null) {
                    Path path = curv.getTotalPath();
                    Paint paint = curv.paint;
                    if (path != null && paint != null) {
                        mCanvasScale.save();
                        mCanvasScale.scale(mMaxScale, mMaxScale);
                        mCanvasScale.drawPath(path, curv.paint);
                        mCanvasScale.restore();
                    }
                }



                currentDrawingMap.remove(id);
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(getClass().getName(), event.toString());
        penDraw(event);
        invalidate();
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mCanvasBitmap, 0, 0, null);

    //测试方法位图的绘制效果：
//        Matrix matrix = new Matrix();
//        matrix.setScale(1f / mMaxScale, 1f / mMaxScale);
//        canvas.drawBitmap(mCanvasScaleBitmap, matrix, null);


        if (isShowTouchEvent) {
            //顺便随手写个多行文本框示例
            float fontSize = 20f;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(1f);
            paint.setTextSize(fontSize);
            //显示触摸事件
            String eventStr[] = touchEventStringBuffer.toString().split("\n");
            for(int i = 0; i < eventStr.length; i++){
                canvas.drawText(eventStr[i], 0, fontSize * (i + 1), paint);
            }
            touchEventStringBuffer = new StringBuffer();
        }
    }
}