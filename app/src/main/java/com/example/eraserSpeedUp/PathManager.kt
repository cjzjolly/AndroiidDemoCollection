package com.example.eraserSpeedUp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import com.example.whiteboard.DrawView.FuntionKind
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class PathManager {
    private var mWidth: Int
    private var mHeight: Int
    private var mPaint: Paint = Paint()
    private val mPrevDirtRange: Rect = Rect()
    private val mDirtRange: Rect = Rect()

    private var mPrevX = 0f
    private var mPrevY = 0f

    private lateinit var mBitmap: Bitmap
    private lateinit var mMyCanvas: Canvas

    constructor(width: Int, height: Int) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mMyCanvas = Canvas(mBitmap)
        mWidth = width
        mHeight = height
    }

    init {
        mPaint.run {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 12f
            strokeCap = Paint.Cap.ROUND
        }
    }

    private val mPathList: ArrayList<Path> = ArrayList()

    /**绘制方式选择 */
    enum class DrawKind {
        NORMAL,  //最普通
        ERASER //橡皮擦
    }

    private var mCurrentCurv: BaseCurv? = null


    /**当前选择的绘制模式 */
    private val mCurrentFunChoice = FuntionKind.DRAW

    /**当前选择的画笔模式 */
    private val mCurrentDrawKind = DrawKind.NORMAL

    /**事件累积 */
    private val touchEventStringBuffer = StringBuffer()

    /**当前正在绘制的线条组合 */
    private val currentDrawingMap: HashMap<Int, BaseCurv> = HashMap()

    var i = 0f
    /**绘制内容到surfaceView**/
    fun draw(canvas: Canvas) {
        //cjztest:
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//        //使用脏区域刷新 //todo cjztest 暂时不是很成功，因为rect之间依然有可能有间隙
//        canvas.drawBitmap(mBitmap, mPrevDirtRange, mPrevDirtRange, null)
//        canvas.drawBitmap(mBitmap, mDirtRange, mDirtRange, null)
//        mPrevDirtRange.set(mDirtRange)
//        //cjztest:
//        canvas.drawRect(mPrevDirtRange, mPaint)
//        canvas.drawRect(mDirtRange, mPaint)

    //        //cjztest 测试图像留存能力 因为双buffer的原因，现象很奇特:
//        canvas.drawCircle(i, i, 30f, mPaint)
//        i += 50
//        Log.i("cjztest", "i == $i")

        //cjzmark todo 暂时使用全局刷新
        canvas.drawBitmap(mBitmap, 0f, 0f, null)
    }

    fun onTouch(event: MotionEvent) {
        val actionType = event.action and MotionEvent.ACTION_MASK
        val paint: Paint = mPaint
        when (actionType) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.i("penDraw_AT", "MotionEvent.ACTION_POINTER_DOWN")
                val id = event.getPointerId(event.actionIndex)
                touchEventStringBuffer.append("MotionEvent.ACTION_DOWN, id:$id\n")
                mCurrentCurv = Curv(paint)
                mCurrentCurv?.draw(event.getX(event.actionIndex), event.getY(event.actionIndex), event.action, mMyCanvas)
                currentDrawingMap[id] = mCurrentCurv!!
                mDirtRange.set(0, 0, mWidth, mHeight)
            }

            MotionEvent.ACTION_DOWN -> {
                val id = event.getPointerId(event.actionIndex)
                touchEventStringBuffer.append("MotionEvent.ACTION_DOWN, id:$id\n")
                when (mCurrentDrawKind) {
                    DrawKind.NORMAL -> mCurrentCurv = Curv(paint)
                    else -> mCurrentCurv = Curv(paint)
                }
                mCurrentCurv?.draw(event.getX(event.actionIndex), event.getY(event.actionIndex), event.action, mMyCanvas)
                currentDrawingMap[id] = mCurrentCurv!!
                mDirtRange.set(0, 0, mWidth, mHeight)
            }

            MotionEvent.ACTION_MOVE -> {
                mDirtRange.setEmpty()
                val x = event.x
                val y = event.y
                val distance = distance(PointF(mPrevX, mPrevY), PointF(x, y)) + paint.strokeWidth * 1.5f
                //设置脏区域
                mDirtRange.set(
                        (x - distance).toInt()
                        ,(y - distance).toInt()
                        ,(x + distance).toInt()
                        ,(y + distance).toInt())
                var i = 0
                while (i < event.pointerCount) {
                    val id = event.getPointerId(i)
                    touchEventStringBuffer.append("MotionEvent.ACTION_MOVE, id:$id\n")
                    val x = event.getX(i)
                    val y = event.getY(i)
                    currentDrawingMap[id]?.draw(x, y, event.action, mMyCanvas)
                    i++
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val id = event.getPointerId(event.actionIndex)
                val curv: BaseCurv? = currentDrawingMap[id]
                mPathList.add(curv?.path!!)
                //清理用过的笔画对象
                currentDrawingMap.remove(id)
            }
        }
        mPrevX = event.x
        mPrevY = event.y
    }

    private fun distance(p0: PointF, p1: PointF): Double {
        return sqrt((p1.x - p0.x).toDouble().pow(2.0)
            + (p1.y - p0.y).toDouble().pow(2.0))
    }
}