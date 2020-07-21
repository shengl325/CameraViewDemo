package com.example.capturebuttondemo.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.capturebuttondemo.R

/**
 * Created by lisheng on 2020/7/20
 */
class CaptureButton(context: Context, attrs: AttributeSet) : View(context) {

    companion object {
        const val STATE_IDLE = 0
        const val STATE_PRESSED = 1
        const val STATE_LONG_PRESSED = 2
        const val STATE_RECORDING = 3
        const val ANIMATION_DURATION = 100L
    }

    private var state: Int

    private var buttonSize: Float

    private var outsideRadius: Float
    private var insideRadius: Float
    private var outsideAddRadius: Float
    private var insideReduceRadius: Float


    init {
        state = STATE_IDLE
        // 初始化按钮尺寸
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CaptureButton)
        buttonSize = ta.getFloat(R.styleable.CaptureButton_size, 0F)
        ta.recycle()
        // 初始化外圆和内圆的半径
        outsideRadius = buttonSize / 2
        insideRadius = buttonSize / 2 * 0.75F
        // 外圆和内圆在动画过程中增加/减少的半径
        outsideAddRadius = buttonSize / 5
        insideReduceRadius = buttonSize / 8
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            (buttonSize + outsideAddRadius * 2).toInt(),
            (buttonSize + outsideAddRadius * 2).toInt()
        )
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
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        handleUnpressedByState()
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
     * 长按动画，动画过程中按钮外圆逐渐增大，内圆逐渐减小。
     * 动画结束后开始录制视频。
     *
     * @param outsideStart 外圆起始半径
     * @param outsideEnd   外圆结束半径
     * @param insideStart  内圆起始半径
     * @param insideEnd    内圆结束半径
     */
    private fun startAnimation(
        outsideStart: Float,
        outsideEnd: Float,
        insideStart: Float,
        insideEnd: Float
    ) {
        val outsideAnim = ValueAnimator.ofFloat(outsideStart, outsideEnd)
        val insideAnim = ValueAnimator.ofFloat(insideStart, insideEnd)
        // 外圆增大动画
        outsideAnim.addUpdateListener {
            outsideRadius = it.animatedValue as Float
            invalidate()
        }
        // 内圆增大动画
        insideAnim.addUpdateListener {
            insideRadius = it.animatedValue as Float
            invalidate()
        }
        val animSet = AnimatorSet()
        animSet.playTogether(outsideAnim, insideAnim)
        animSet.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    if (state == STATE_LONG_PRESSED) {
                        state = STATE_RECORDING
                        // TODO: 开始录像。
                    }
                }
            }
        )
        animSet.duration = ANIMATION_DURATION
        animSet.start()
    }


}
