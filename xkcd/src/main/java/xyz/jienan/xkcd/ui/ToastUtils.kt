package xyz.jienan.xkcd.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import xyz.jienan.xkcd.R

object ToastUtils {

    private var toast: Toast? = null

    @SuppressLint("ShowToast")
    fun showToast(context: Context, text: String, position : Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, duration: Int = Toast.LENGTH_SHORT) {
        try {
            toast!!.view?.isShown
            toast!!.setText(text)
        } catch (e: Exception) {
            toast = Toast.makeText(context.applicationContext, text, duration)
        }

        if (position != Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL) {
            toast!!.setGravity(position, 0, 0)
        }

        val textView = toast!!.view?.findViewById<TextView>(android.R.id.message)
        if (textView != null) {
            textView.typeface = ResourcesCompat.getFont(context, R.font.xkcd)
        }
        toast!!.show()
    }

    fun cancelToast() {
        if (toast != null && toast!!.view?.isShown == true) {
            toast!!.cancel()
        }
    }
}