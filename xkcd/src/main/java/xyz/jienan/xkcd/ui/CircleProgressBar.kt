package xyz.jienan.xkcd.ui

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import xyz.jienan.xkcd.R
import kotlin.math.min


class CircleProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), IProgressbar {

    private val mProgressRectF = RectF()

    private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mRadius = 0f
    private var mCenterX = 0f
    private var mCenterY = 0f

    override var progress: Int = 0
        set(progress) {
            field = progress
            invalidate()
        }

    //Stroke width of the progress of the progress bar
    private var mProgressStrokeWidth = 0f

    //Start color of the progress of the progress bar
    private var mProgressStartColor = 0
    //End color of the progress of the progress bar
    private var mProgressEndColor = 0

    //Background color of the progress of the progress bar
    private var mProgressBackgroundColor = 0

    init {
        initFromAttributes(context, attrs)
        initPaint()
    }

    /**
     * Basic data initialization
     */
    private fun initFromAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)

        mProgressStrokeWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_progress_stroke_width, dp2px(getContext(), DEFAULT_PROGRESS_STROKE_WIDTH)).toFloat()
        mProgressStartColor = a.getColor(R.styleable.CircleProgressBar_progress_start_color, Color.parseColor(COLOR_FFF2A670))
        mProgressEndColor = a.getColor(R.styleable.CircleProgressBar_progress_end_color, Color.parseColor(COLOR_FFF2A670))
        mProgressBackgroundColor = a.getColor(R.styleable.CircleProgressBar_progress_background_color, Color.parseColor(COLOR_FFD3D3D5))

        a.recycle()
    }

    /**
     * Paint initialization
     */
    private fun initPaint() {
        mProgressPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mProgressStrokeWidth
            color = mProgressStartColor
            strokeCap = Paint.Cap.BUTT
        }

        mProgressBackgroundPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mProgressStrokeWidth
            color = mProgressBackgroundColor
            strokeCap = Paint.Cap.BUTT
        }
    }

    /**
     * The progress bar color gradient,
     * need to be invoked in the [.onSizeChanged]
     */
    private fun updateProgressShader() {
        if (mProgressStartColor != mProgressEndColor) {
            val shader = LinearGradient(mProgressRectF.left, mProgressRectF.top,
                    mProgressRectF.left, mProgressRectF.bottom,
                    mProgressStartColor, mProgressEndColor, Shader.TileMode.CLAMP)
            val matrix = Matrix()
            matrix.setRotate(LINEAR_START_DEGREE, mCenterX, mCenterY)
            shader.setLocalMatrix(matrix)

            mProgressPaint.shader = shader
        } else {
            mProgressPaint.shader = null
            mProgressPaint.color = mProgressStartColor
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.rotate(DEFAULT_START_DEGREE, mCenterX, mCenterY)
        drawProgress(canvas)
        canvas.restore()
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.drawArc(mProgressRectF, 0.0f, MAX_DEGREE, false, mProgressBackgroundPaint)
        canvas.drawArc(mProgressRectF, 0.0f, MAX_DEGREE * progress / 100, false, mProgressPaint)
    }

    /**
     * When the size of CircleProgressBar changed, need to re-adjust the drawing area
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterX = (w / 2).toFloat()
        mCenterY = (h / 2).toFloat()

        mRadius = min(mCenterX, mCenterY)
        mProgressRectF.top = mCenterY - mRadius
        mProgressRectF.bottom = mCenterY + mRadius
        mProgressRectF.left = mCenterX - mRadius
        mProgressRectF.right = mCenterX + mRadius

        updateProgressShader()

        //Prevent the progress from clipping
        mProgressRectF.inset(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2)
    }

    private class SavedState : BaseSavedState {
        internal var progress: Int = 0

        internal constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            progress = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(progress)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        // Force our ancestor class to save its state
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply { progress = this@CircleProgressBar.progress }
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        progress = ss.progress
    }

    companion object {
        private const val MAX_DEGREE = 360.0f
        private const val LINEAR_START_DEGREE = 90.0f

        private const val DEFAULT_START_DEGREE = -90f
        private const val DEFAULT_PROGRESS_STROKE_WIDTH = 1.0f

        private const val COLOR_FFF2A670 = "#fff2a670"
        private const val COLOR_FFD3D3D5 = "#ffe3e3e5"
    }
}