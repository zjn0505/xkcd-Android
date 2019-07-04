package xyz.jienan.xkcd.ui.xkcdimageview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.hypot

class DragImageView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, defStyle: Int = 0)
    : BigImageView(context, attr, defStyle) {

    lateinit var onExitListener: () -> Unit

    private var downX = 0f

    private var downY = 0f

    private var translateX = 0f

    private var translateY = 0f

    private var scale = 1f

    private var newAlpha = 1f

    private var oneFingerTouch = true

    private var firstPointerId = 0

    override fun dispatchDraw(canvas: Canvas) {
        canvas.translate(translateX, translateY)
        canvas.scale(scale, scale, downX + translateX, downY + translateY)
        alpha = newAlpha
        super.dispatchDraw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        if (isOriginalSized) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onActionDown(event)
                MotionEvent.ACTION_MOVE -> {
                    return if (firstPointerId == event.getPointerId(event.actionIndex) && (event.pointerCount == 1 || scale < 1f)) {
                        onActionMove(event)
                        true
                    } else if (event.pointerCount > 1 && scale != 1f) {
                        false
                    } else {
                        super.dispatchTouchEvent(event)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    onActionUp()
                    oneFingerTouch = true
                    return if (translateY == 0f) {
                        super.dispatchTouchEvent(event)
                    } else {
                        true
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun onActionDown(event: MotionEvent) {
        firstPointerId = event.getPointerId(event.actionIndex)
        downX = event.x
        downY = event.y
    }

    private fun onActionMove(event: MotionEvent) {
        translateX = event.x - downX
        translateY = event.y - downY

        val percent = abs(translateY) / height

        if (scale in MIN_SCALE..1f) {
            scale = (1 - percent).coerceIn(MIN_SCALE, 1f)
            newAlpha = (1 - 1.5f * percent).coerceIn(0f, 1f)
        }
        invalidate()
    }

    private fun onActionUp() {
        if (hypot(translateX, translateY) > MAX_TRANSLATE_Y) {
            onExitListener.invoke()
        } else {
            resumeAnimators.map { it.duration = DURATION; it.start() }
        }
    }

    private val resumeAnimators
        get() = arrayOf(alphaAnimator, translateYAnimator, translateXAnimator, scaleAnimator)

    private val alphaAnimator
        get() = ValueAnimator.ofFloat(newAlpha, 1f).apply {
            addUpdateListener { newAlpha = animatedValue as Float }
        }

    private val translateYAnimator
        get() = ValueAnimator.ofFloat(translateY, 0f).apply {
            addUpdateListener { translateY = animatedValue as Float }
        }

    private val translateXAnimator
        get() = ValueAnimator.ofFloat(translateX, 0f).apply {
            addUpdateListener { translateX = animatedValue as Float }
        }

    private val scaleAnimator
        get() = ValueAnimator.ofFloat(scale, 1f).apply {
            addUpdateListener {
                scale = animatedValue as Float
                invalidate()
            }
        }

    companion object {

        private const val MAX_TRANSLATE_Y = 400

        private const val MIN_SCALE = 0.5f

        private const val DURATION: Long = 300
    }
}