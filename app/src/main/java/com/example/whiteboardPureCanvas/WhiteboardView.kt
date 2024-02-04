package com.example.whiteboardPureCanvas

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.example.whiteboard.BaseCurv
import com.example.whiteboard.Curv
import com.example.whiteboard.CurvEraser
import com.example.whiteboard.CurvPenMode
import java.util.*
import kotlin.collections.HashMap

/**1、移动和缩放时缩放是针对画布而不是越来越多的path，使得计算量保持一定
 * 2、通过反向缩放、反向移动实现绘制的在图片上时不会出现绘制中的path偏移
 * 3、擦除使用先判断线条两端、线拐点是否通过擦除范围的技巧进行提速，另外还有合理使用多线程**/
class WhiteboardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mCurrentCurv: BaseCurv? = null

    /**控件长宽 */
    private var mWidth = 0
    /**控件长宽 */
    private var mHeight = 0
    /**进行过初始化了吗 */
    private var isInitFinished = false

    /**当前绘制画布 */
    private var mCanvasBitmap: Bitmap? = null

    /**当前绘制画布 */
    private var mCanvas: Canvas? = null

    /**绘制方式选择 */
    enum class DrawKind {
        NORMAL,  //最普通
        PEN,  //笔锋
        ERASER //橡皮擦
    }

    /**功能选择 */
    enum class FuntionKind {
        DRAW, MOVE_AND_SCALE
    }

    /**当前选择的绘制模式 */
    private var mCurrentFunChoice = FuntionKind.DRAW

    /**当前选择的画笔模式 */
    private val mCurrentDrawKind = DrawKind.NORMAL

    /**事件累积 */
    private var touchEventStringBuffer = StringBuffer()

    /**当前正在绘制的线条组合 */
    private val currentDrawingMap: MutableMap<Int, BaseCurv> = HashMap()

    /**是否绘制触摸 */
    private val isShowTouchEvent = true

    private val mPathList = LinkedList<BaseCurv>()

    private val mPrevTouch = PointF()

    fun setMode(mode : FuntionKind) {
        mCurrentFunChoice = mode
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (mCurrentFunChoice) {
                FuntionKind.DRAW -> {
                    penDraw(it)
                }
                FuntionKind.MOVE_AND_SCALE -> {
                    translate(it)
                }
            }
        }
        invalidate()
        return true
    }

    /**获取绘制笔 */
    private fun makePaint(): Paint {
        val paint = Paint()
        paint.strokeWidth = 12f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
        var color = -0x1000000
        //随机颜色
        color = color or ((Math.random() * 255 + 1).toInt() shl 16)
        color = color or ((Math.random() * 255 + 1).toInt() shl 8)
        color = color or (Math.random() * 255 + 1).toInt()
        paint.color = color
        return paint
    }

    /**书写 */
    private fun penDraw(event: MotionEvent) {
        val actionType = event.action and MotionEvent.ACTION_MASK
        when (actionType) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.i("penDraw_AT", "MotionEvent.ACTION_POINTER_DOWN")
                val id = event.getPointerId(event.actionIndex)
                touchEventStringBuffer.append("MotionEvent.ACTION_DOWN, id:$id\n")
                val paint: Paint = makePaint()
                when (mCurrentDrawKind) {
                    DrawKind.NORMAL -> mCurrentCurv = Curv(paint)
                    DrawKind.PEN -> mCurrentCurv = CurvPenMode(paint)
                    DrawKind.ERASER -> mCurrentCurv = CurvEraser(paint)
                    else -> mCurrentCurv = Curv(paint)
                }
                mCurrentCurv?.let {
                    it.draw(event.getX(event.actionIndex), event.getY(event.actionIndex), event.action, mCanvas)
                    currentDrawingMap.put(id, it)
                }
                mCurrentCurv?.let {  mPathList.add(it) }
            }
            MotionEvent.ACTION_DOWN -> {
                val id = event.getPointerId(event.actionIndex)
                touchEventStringBuffer.append("MotionEvent.ACTION_DOWN, id:$id\n")
                val paint: Paint = makePaint()
                when (mCurrentDrawKind) {
                    DrawKind.NORMAL -> mCurrentCurv = Curv(paint)
                    DrawKind.PEN -> mCurrentCurv = CurvPenMode(paint)
                    DrawKind.ERASER -> mCurrentCurv = CurvEraser(paint)
                    else -> mCurrentCurv = Curv(paint)
                }
                mCurrentCurv?.let {
                    it.draw(event.getX(event.actionIndex), event.getY(event.actionIndex), event.action, mCanvas)
                    currentDrawingMap.put(id, it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    touchEventStringBuffer.append("MotionEvent.ACTION_MOVE, id:$id\n")
                    currentDrawingMap[id]?.draw(event.getX(i), event.getY(i), event.action, mCanvas)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val id = event.getPointerId(event.actionIndex)
                val curv: BaseCurv? = currentDrawingMap[id]
                curv?.let {

                }
                //清理用过的笔画对象
                currentDrawingMap.remove(id)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun translate(event: MotionEvent) {
        //todo 使用同一张图片移动迭代，例如将当前bmp内容左移一点后再写到bmp中
        if (mCanvasBitmap == null) {
            return
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPrevTouch.set(event.x, event.y)
            }
        }
        if (!mCanvasBitmap!!.isMutable || mCanvasBitmap!!.isRecycled) {
            return
        }

        val canvas = Canvas(mCanvasBitmap!!)
        //设置完全覆盖模式的绘制方式，否则不会清屏
        val paint = Paint()
        paint.blendMode = BlendMode.SRC
        val dx = event.x - mPrevTouch.x
        val dy = event.y - mPrevTouch.y
        Log.e("cjztest", "dx:$dx, dy:$dy")
        canvas.run {
            val contentBmp = mCanvasBitmap!!.copy(mCanvasBitmap!!.config, true)
            drawBitmap(contentBmp, dx, dy, paint)
            contentBmp.recycle()

//            clipOutRect(3f, 0f, width.toFloat() - 3, height.toFloat())
//            drawColor(Color.RED, PorterDuff.Mode.SRC) //for debug
//            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        mPrevTouch.set(event.x, event.y)

    }

    override fun onDraw(canvas: Canvas?) {
        mCanvasBitmap?.let {bmp ->
            canvas?.drawBitmap(bmp, 0f, 0f, null)
        }

        if (isShowTouchEvent) {
            //顺便随手写个多行文本框示例
            val fontSize = 20f
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.isAntiAlias = true
            paint.color = Color.RED
            paint.strokeWidth = 1f
            paint.textSize = fontSize
            //显示触摸事件
            val eventStr = touchEventStringBuffer.toString().split("\n").toTypedArray()
            for (i in eventStr.indices) {
                canvas?.drawText(eventStr[i], 0f, fontSize * (i + 1), paint)
            }
            touchEventStringBuffer = StringBuffer()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (width == 0 || height == 0) {
            return
        }
        if (!isInitFinished || mWidth != width || mHeight != height) {
            mWidth = width
            mHeight = height
            mCanvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
            mCanvasBitmap?.let {
                mCanvas = Canvas(it)
            }
            isInitFinished = true
        }

    }
}