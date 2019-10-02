package xyz.jienan.xkcd.model


import androidx.core.text.HtmlCompat
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Uid
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils
import java.io.Serializable

/**
 * Created by jienanzhang on 09/07/2017.
 */

@Entity
data class XkcdPic constructor(
        val year: String = "",
        val month: String = "",
        val day: String = "",
        @Id(assignable = true)
        var num: Long = 0L,
        @Uid(9035471003790175147L)
        @SerializedName("alt")
        val _alt: String = "",
        var width: Int = 0,
        var height: Int = 0,
        var isFavorite: Boolean = false,
        var hasThumbed: Boolean = false,
        @io.objectbox.annotation.Transient
        val thumbCount: Long = 0L,
        @Uid(7047913805660868881L)
        @SerializedName("title")
        val _title: String = "",
        var img: String = "",
        @io.objectbox.annotation.Transient
        @Transient
        val translated: Boolean = false) : Serializable {

    val targetImg: String
        get() = if (translated) {
            img
        } else {
            XkcdSideloadUtils.sideload(this).img
        }

    val title
        get() = HtmlCompat.fromHtml(_title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    val alt
        get() = HtmlCompat.fromHtml(_alt, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    override fun equals(other: Any?) = other is XkcdPic && this.num == other.num

    override fun hashCode() = super.hashCode()
}
