package xyz.jienan.xkcd.model

import android.text.TextUtils

import androidx.annotation.Keep

import xyz.jienan.xkcd.Const.TAG_XKCD

/**
 * author : Stacey's dad
 * content : Get out while you still can
 * num : 61
 * source : xkcd
 */

@Keep
data class Quote(var author: String = "Man in Chair",
                 var content: String = "Sudo make me a sandwich",
                 var num: Int = 149,
                 var source: String = TAG_XKCD,
                 var timestamp: Long = System.currentTimeMillis()) {

    override fun equals(other: Any?): Boolean {
        if (other is Quote) {
            val q = other as Quote?
            return (q!!.num == this.num
                    && !TextUtils.isEmpty(q.content)
                    && q.content == this.content)
        }
        return false
    }

    override fun hashCode() = super.hashCode()
}
