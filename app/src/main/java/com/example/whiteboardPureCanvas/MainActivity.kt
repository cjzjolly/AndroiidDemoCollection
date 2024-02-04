package com.example.whiteboardPureCanvas

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val frameLayout = FrameLayout(this)
        val whiteBoard = WhiteboardView(this)
        val ll = LinearLayout(this)
        frameLayout.addView(whiteBoard)
        frameLayout.addView(ll)
        ll.run {
            orientation = LinearLayout.VERTICAL
            addView(Button(this@MainActivity).apply {
                text = "绘制"
                setOnClickListener { whiteBoard.setMode(WhiteboardView.FuntionKind.DRAW) }
            })
            addView(Button(this@MainActivity).apply {
                text = "移动缩放"
                setOnClickListener { whiteBoard.setMode(WhiteboardView.FuntionKind.MOVE_AND_SCALE) }
            })
        }
        setContentView(frameLayout)
    }
}