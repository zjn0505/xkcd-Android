package xyz.jienan.xkcd.ui

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(private val recyclerView: RecyclerView, private val mListener: OnItemClickListener?)
    : RecyclerView.SimpleOnItemTouchListener() {

    private val mGestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            val child = recyclerView.findChildViewUnder(e.x, e.y)
            if (child != null && mListener != null) {
                mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
            }
        }
    })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
            return true
        }
        return false
    }

    abstract class OnItemClickListener {

        open fun onItemClick(view: View, position: Int) {}

        open fun onLongItemClick(view: View, position: Int) {}
    }
}
