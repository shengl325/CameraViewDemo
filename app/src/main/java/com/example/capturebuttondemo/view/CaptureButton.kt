package com.example.capturebuttondemo.view

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View

/**
 * Created by lisheng on 2020/7/20
 */
class CaptureButton(context: Context) : View(context) {

    companion object {
        const val STATE_IDLE = 0
        const val STATE_PRESSED = 1
        const val STATE_LONG_PRESSED = 2
        const val STATE_RECORDING = 3
    }

    private var state: Int

    init {
        state = STATE_IDLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 暂时把长按阈值写死，后续改为可配置
                state = STATE_PRESSED
                postDelayed(longPressedRunnable, 500)
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {
                handleUnpressedByState()
            }
        }
        return true
    }

    // 长按任务
    private val longPressedRunnable = Runnable {
        state = STATE_LONG_PRESSED

    }

    private fun handleUnpressedByState() {
        removeCallbacks(longPressedRunnable)


    }

    /**
     * 长按动画
     */
    private fun startAnimation() {

    }
}
