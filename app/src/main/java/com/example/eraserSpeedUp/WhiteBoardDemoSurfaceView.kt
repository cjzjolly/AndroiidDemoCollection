package com.example.eraserSpeedUp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class WhiteBoardDemoSurfaceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var mSurfaceHolder: SurfaceHolder = holder
    private var mPathManager: PathManager? = null

    init {
        mSurfaceHolder.addCallback(this)
        mSurfaceHolder.setFormat(PixelFormat.RGBA_8888)
        isFocusable = true
        setFocusableInTouchMode(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            mPathManager?.onTouch(this)
            draw()
//            Log.i("cjztest", "touch")
        }
        return true
    }

    fun draw() {
        mSurfaceHolder.run {
            val canvas = lockCanvas()
            mPathManager?.draw(canvas)
            unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mPathManager = PathManager(width, height)
        Log.i("cjztest", "surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
}