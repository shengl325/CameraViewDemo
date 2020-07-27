package com.example.capturebuttondemo.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.capturebuttondemo.R

/**
 * Created by lisheng on 2020/7/20
 */
class CaptureButton(context: Context, attrs: AttributeSet) : View(context) {

    companion object {
        private const val STATE_IDLE = 0
        private const val STATE_PRESSED = 1
        private const val STATE_LONG_PRESSED = 2
        private const val STATE_RECORDING = 3

        private const val ANIMATION_DURATION = 100L     //长按短按动画时长
        private const val MAX_RECORD_DURATION = 10000L  //录制最大时长
    }

    private var state: Int

    private var mPaint: Paint = Paint()

    private var buttonSize: Float          //控件大小
    private var strokeWidth: Float         //进度条宽度
    private var outsideRadius: Float       //外圆半径
    private var insideRadius: Float        //内圆半径
    private var outsideAddRadius: Float    //长按动画师外圆增加的半径
    private var insideReduceRadius: Float  //长按动画时内圆减少的半径

    private var centerX: Float
    private var centerY: Float

    private var progressBarColor: Int = 0xEE16AE16.toInt()  //进度条颜色
    private var outsideColor: Int = 0XEECCCCCC.toInt()      //外圆颜色
    private var insideColor: Int = 0XFFFFFFFF.toInt()       //内圆颜色

    private var progress: Float = 0F  //录制进度
    private var rectF: RectF

    private val recordTimer: RecordCountDownTimer

    init {
        state = STATE_IDLE
        //初始化按钮尺寸
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CaptureButton)
        buttonSize = ta.getFloat(R.styleable.CaptureButton_size, 0F)
        ta.recycle()
        //内外圆半径，内外圆动画过程中变化的半径，进度条宽度
        outsideRadius = buttonSize / 2
        insideRadius = buttonSize / 2 * 0.75F
        outsideAddRadius = buttonSize / 5
        insideReduceRadius = buttonSize / 8
        strokeWidth = buttonSize / 15
        //控件中心坐标
        centerX = (buttonSize + outsideAddRadius * 2) / 2
        centerY = (buttonSize + outsideAddRadius * 2) / 2
        //进度条范围
        rectF = RectF(
            centerX - (buttonSize / 2 + outsideAddRadius - strokeWidth / 2),
            centerY + (buttonSize / 2 + outsideAddRadius - strokeWidth / 2),
            centerX + (buttonSize / 2 + outsideAddRadius - strokeWidth / 2),
            centerY - (buttonSize / 2 + outsideAddRadius - strokeWidth / 2)
        )

        recordTimer = RecordCountDownTimer(
            MAX_RECORD_DURATION,
            MAX_RECORD_DURATION / 360
        )

        mPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            (buttonSize + outsideAddRadius * 2).toInt(),
            (buttonSize + outsideAddRadius * 2).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画外圆
        mPaint.style = Paint.Style.FILL
        mPaint.color = outsideColor
        canvas.drawCircle(centerX, centerY, outsideRadius, mPaint)
        //画内圆
        mPaint.color = insideColor
        canvas.drawCircle(centerX, centerY, insideRadius, mPaint)
        //如果按钮处于正在录制视频的状态)，则绘制进度条。
        if (state == STATE_RECORDING) {
            mPaint.color = progressBarColor
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = strokeWidth
            canvas.drawArc(rectF, -90F, progress, false, mPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //暂时把长按阈值写死, 后续改为可配置
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
        preRecordAnimation(
            outsideRadius,
            outsideRadius + outsideAddRadius,
            insideRadius,
            insideRadius - insideReduceRadius
        )
    }

    private fun handleUnpressedByState() {
        removeCallbacks(longPressedRunnable)
        when (state) {
            STATE_PRESSED -> {
                captureAnimation()
            }
            STATE_LONG_PRESSED -> {

            }
            STATE_RECORDING -> {

            }
        }
    }

    /**
     * 拍照动画。
     */
    private fun captureAnimation() {

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
    private fun preRecordAnimation(
        outsideStart: Float,
        outsideEnd: Float,
        insideStart: Float,
        insideEnd: Float
    ) {
        val outsideAnim = ValueAnimator.ofFloat(outsideStart, outsideEnd)
        val insideAnim = ValueAnimator.ofFloat(insideStart, insideEnd)
        //外圆增大动画
        outsideAnim.addUpdateListener {
            outsideRadius = it.animatedValue as Float
            invalidate()
        }
        //内圆增大动画
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

    fun updateProgress(millisUntilFinished: Long) {
        progress = 360F - millisUntilFinished / MAX_RECORD_DURATION.toFloat() * 360
        invalidate()
    }

    inner class RecordCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            updateProgress(0)
        }

        override fun onTick(millisUntilFinished: Long) {
            updateProgress(millisUntilFinished)
        }
    }
}
