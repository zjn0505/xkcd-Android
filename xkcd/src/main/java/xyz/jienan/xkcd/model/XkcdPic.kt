package xyz.jienan.xkcd.model


import androidx.core.text.HtmlCompat
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.NameInDb
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils

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
        @NameInDb("alt")
        @SerializedName("alt")
        val _alt: String = "",
        var width: Int = 0,
        var height: Int = 0,
        var isFavorite: Boolean = false,
        var hasThumbed: Boolean = false,
        val thumbCount: Long = 0L,
        @NameInDb("title")
        @SerializedName("title")
        val _title: String = "",
        var img: String = "") {

    val targetImg: String
        get() = XkcdSideloadUtils.sideload(this).img

    val title
        get() = HtmlCompat.fromHtml(_title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    val alt
        get() = HtmlCompat.fromHtml(_alt, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    override fun equals(other: Any?) = other is XkcdPic && this.num == other.num

    override fun hashCode() = super.hashCode()
}
