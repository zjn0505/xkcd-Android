package xyz.jienan.xkcd.list

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import xyz.jienan.xkcd.R

class RecyclerViewFastScroller @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        LinearLayout(context, attrs, defStyleAttr) {

    private val bubble by lazy { findViewById<TextView>(R.id.fastscroller_bubble) }

    private val handle by lazy { findViewById<View>(R.id.fastscroller_handle) }

    private var recyclerView: RecyclerView? = null

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            updateBubbleAndHandlePosition()
        }
    }

    private var currentAnimator: ObjectAnimator? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.rv_scroller, this, true)
        orientation = HORIZONTAL
        clipChildren = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBubbleAndHandlePosition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < handle!!.x - ViewCompat.getPaddingStart(handle!!))
                    return false
                if (currentAnimator != null)
                    currentAnimator!!.cancel()
                if (bubble != null && bubble!!.visibility == View.INVISIBLE)
                    showBubble()
                handle!!.isSelected = true
                val y = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handle!!.isSelected = false
                hideBubble()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        if (this.recyclerView !== recyclerView) {
            if (this.recyclerView != null)
                this.recyclerView!!.removeOnScrollListener(onScrollListener)
            this.recyclerView = recyclerView
            if (this.recyclerView == null)
                return
            recyclerView.addOnScrollListener(onScrollListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (recyclerView != null) {
            recyclerView!!.removeOnScrollListener(onScrollListener)
            recyclerView = null
        }
    }

    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView != null) {
            val itemCount = recyclerView!!.adapter!!.itemCount

            val proportion = when {
                handle!!.y == 0f -> 0f
                handle!!.y + handle!!.height >= height - TRACK_SNAP_RANGE -> 1f
                else -> y / height.toFloat()
            }

            val targetPos = getValueInRange(0, itemCount - 1, (proportion * itemCount.toFloat()).toInt())

            val layoutManager = recyclerView!!.layoutManager

            if (layoutManager is StaggeredGridLayoutManager) {
                layoutManager.scrollToPositionWithOffset(targetPos, 0)
            } else if (layoutManager is LinearLayoutManager) {
                layoutManager.scrollToPositionWithOffset(targetPos, 0)
            }

            bubble.text = (recyclerView!!.adapter as BubbleTextGetter).getTextToShowInBubble(targetPos)
        }
    }

    private fun getValueInRange(min: Int, max: Int, value: Int) = value.coerceIn(min, max)

    private fun updateBubbleAndHandlePosition() {
        if (bubble == null || handle!!.isSelected)
            return

        val verticalScrollOffset = recyclerView!!.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView!!.computeVerticalScrollRange()
        val proportion = verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - height)
        setBubbleAndHandlePosition(height * proportion)
    }

    private fun setBubbleAndHandlePosition(y: Float) {
        val handleHeight = handle!!.height
        handle!!.y = (y - handleHeight / 2).coerceIn(0f, (height - handleHeight).toFloat())
        val bubbleHeight = bubble!!.height
        bubble!!.y = (y - bubbleHeight).coerceIn(0f, height - bubbleHeight - handleHeight / 2f)
    }

    private fun showBubble() {
        bubble!!.visibility = View.VISIBLE

        if (currentAnimator != null)
            currentAnimator!!.cancel()

        currentAnimator = ObjectAnimator.ofFloat(bubble, View.ALPHA, 0f, 1f)
                .setDuration(BUBBLE_ANIMATION_DURATION).apply { start() }
    }

    private fun hideBubble() {
        if (currentAnimator != null)
            currentAnimator!!.cancel()

        currentAnimator = ObjectAnimator.ofFloat(bubble, View.ALPHA, 1f, 0f)
                .setDuration(BUBBLE_ANIMATION_DURATION)
                .apply {
                    doOnEnd {
                        bubble!!.visibility = View.INVISIBLE
                        currentAnimator = null
                    }
                    doOnCancel {
                        bubble!!.visibility = View.INVISIBLE
                        currentAnimator = null
                    }
                    start()
                }
    }

    interface BubbleTextGetter {
        fun getTextToShowInBubble(pos: Int): String
    }

    companion object {
        private const val BUBBLE_ANIMATION_DURATION = 100L
        private const val TRACK_SNAP_RANGE = 5
    }
}