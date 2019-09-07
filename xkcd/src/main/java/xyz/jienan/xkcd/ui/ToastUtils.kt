package xyz.jienan.xkcd.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {

    private var toast: Toast? = null

    @SuppressLint("ShowToast")
    fun showToast(context: Context, text: String, position : Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL) {
        try {
            toast!!.view.isShown
            toast!!.setText(text)
        } catch (e: Exception) {
            toast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT)
        }

        if (position != Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL) {
            toast!!.setGravity(position, 0, 0)
        }

        toast!!.show()
    }

    fun cancelToast() {
        if (toast != null && toast!!.view.isShown) {
            toast!!.cancel()
        }
    }
}