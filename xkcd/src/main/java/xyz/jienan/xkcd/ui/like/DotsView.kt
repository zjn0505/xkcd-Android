package xyz.jienan.xkcd.ui.like

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Miroslaw Stanek on 21.12.2015.
 * Modified by Joel Dean
 */
class DotsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val circlePaints = Array(4) { Paint().apply { style = Paint.Style.FILL; isAntiAlias = true } }

    private val colors = arrayOf(COLOR_1, COLOR_2, COLOR_3, COLOR_4)

    private var pWidth = 0

    private var pHeight = 0

    private val maxOuterDotsRadius
        get() = width / 2 - maxDotSize * 2

    private val maxInnerDotsRadius
        get() = 0.8f * maxOuterDotsRadius

    private val maxDotSize = 5f

    var currentProgress = 0f
        set(currentProgress) {
            field = currentProgress
            updateDotsPaints()
            updateDotsAlpha()
            postInvalidate()
        }

    private val currentRadius1
        get() = if (currentProgress < 0.3f) {
            LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.0, 0.3, 0.0, (maxOuterDotsRadius * 0.8f).toDouble()).toFloat()
        } else {
            LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.3, 1.0, maxInnerDotsRadius.toDouble(), maxOuterDotsRadius.toDouble()).toFloat()
        }
    private val currentDotSize1
        get() = when {
            currentProgress == 0f -> 0f
            currentProgress < 0.7 -> maxDotSize
            else -> LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.7, 1.0, maxDotSize.toDouble(), 0.0).toFloat()
        }

    private val currentDotSize2
        get() = when {
            currentProgress == 0f -> 0f
            currentProgress < 0.2 -> maxDotSize
            currentProgress < 0.5 -> LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.2, 0.5, maxDotSize.toDouble(), 0.3 * maxDotSize).toFloat()
            else -> LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.5, 1.0, (maxDotSize * 0.3f).toDouble(), 0.0).toFloat()
        }

    private val currentRadius2
        get() = if (currentProgress < 0.3f) {
            LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.0, 0.3, 0.0, maxInnerDotsRadius.toDouble()).toFloat()
        } else {
            maxInnerDotsRadius
        }

    private val argbEvaluator = ArgbEvaluator()

    override fun onDraw(canvas: Canvas) {
        drawOuterDotsFrame(canvas)
        drawInnerDotsFrame(canvas)
    }

    private fun drawOuterDotsFrame(canvas: Canvas) {
        val centerX = canvas.width / 2
        val r1 = currentRadius1
        val size1 = currentDotSize1
        for (i in 0 until DOTS_COUNT) {
            val cX = (centerX + r1 * cos(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180)).toInt()
            val cY = (centerX + r1 * sin(i * OUTER_DOTS_POSITION_ANGLE * Math.PI / 180)).toInt()
            canvas.drawCircle(cX.toFloat(), cY.toFloat(), size1, circlePaints[i % circlePaints.size])
        }
    }

    private fun drawInnerDotsFrame(canvas: Canvas) {
        val centerX = canvas.width / 2
        val r2 = currentRadius2
        val size2 = currentDotSize2
        for (i in 0 until DOTS_COUNT) {
            val cX = (centerX + r2 * cos((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180)).toInt()
            val cY = (centerX + r2 * sin((i * OUTER_DOTS_POSITION_ANGLE - 10) * Math.PI / 180)).toInt()
            canvas.drawCircle(cX.toFloat(), cY.toFloat(), size2, circlePaints[(i + 1) % circlePaints.size])
        }
    }

    private fun updateDotsPaints() {
        if (currentProgress < 0.5f) {
            val progress = LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.0, 0.5, 0.0, 1.0).toFloat()
            circlePaints.forEachIndexed { index, paint -> paint.color = argbEvaluator.evaluate(progress, colors[index], colors[(index + 1) % 4]) as Int }
        } else {
            val progress = LikeUtils.mapValueFromRangeToRange(currentProgress.toDouble(), 0.5, 1.0, 0.0, 1.0).toFloat()
            circlePaints.forEachIndexed { index, paint -> paint.color = argbEvaluator.evaluate(progress, colors[(index + 1) % 4], colors[(index + 2) % 4]) as Int }
        }
    }

    private fun updateDotsAlpha() {
        val progress = LikeUtils.clamp(currentProgress.toDouble(), 0.6, 1.0).toFloat()
        val alpha = LikeUtils.mapValueFromRangeToRange(progress.toDouble(), 0.6, 1.0, 255.0, 0.0).toInt()
        circlePaints.forEach { it.alpha = alpha }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (pWidth != 0 && pHeight != 0)
            setMeasuredDimension(pWidth, pHeight)
    }

    fun setSize(width: Int, height: Int) {
        pWidth = width
        pHeight = height
        invalidate()
    }

    companion object {
        private const val DOTS_COUNT = 7
        private const val OUTER_DOTS_POSITION_ANGLE = 51

        private const val COLOR_1 = 0xFFC107
        private const val COLOR_2 = 0xFF9800
        private const val COLOR_3 = 0xFF5722
        private const val COLOR_4 = 0xF44336
    }
}