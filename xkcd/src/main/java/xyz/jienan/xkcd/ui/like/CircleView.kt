package xyz.jienan.xkcd.ui.like

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.createBitmap

/**
 * Created by Miroslaw Stanek on 21.12.2015.
 * Modified by Joel Dean
 */

class CircleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val argbEvaluator = ArgbEvaluator()

    private val circlePaint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = true }

    private val maskPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR); isAntiAlias = true }

    private lateinit var tempBitmap: Bitmap

    private lateinit var tempCanvas: Canvas

    var outerCircleRadiusProgress = 0f
        set(outerCircleRadiusProgress) {
            field = outerCircleRadiusProgress
            updateCircleColor()
            postInvalidate()
        }

    var innerCircleRadiusProgress = 0f
        set(innerCircleRadiusProgress) {
            field = innerCircleRadiusProgress
            postInvalidate()
        }

    private val size = arrayOf(0, 0)

    fun setSize(width: Int, height: Int) {
        size[0] = width
        size[1] = height
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (size[0] != 0 && size[1] != 0)
            setMeasuredDimension(size[0], size[1])
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        tempBitmap = createBitmap(width, width)
        tempCanvas = Canvas(tempBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        tempCanvas.drawColor(0xffffff, PorterDuff.Mode.CLEAR)
        tempCanvas.drawCircle(width / 2f, height / 2f, outerCircleRadiusProgress * width / 2, circlePaint)
        tempCanvas.drawCircle(width / 2f, height / 2f, innerCircleRadiusProgress * width / 2 + 1, maskPaint)
        canvas.drawBitmap(tempBitmap, 0f, 0f, null)
    }

    private fun updateCircleColor() {
        var colorProgress = outerCircleRadiusProgress.coerceIn(0.5f, 1.0f)
        colorProgress = colorProgress.mapValueFromRangeToRange(0.5f, 1.0f, 0.0f, 1.0f)
        circlePaint.color = argbEvaluator.evaluate(colorProgress, 0xFF5722, 0xFFC107) as Int
    }
}
