package xyz.jienan.xkcd.comics

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.cursoradapter.widget.CursorAdapter

import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.glide.XkcdGlideUtils

/**
 * Created by jienanzhang on 21/03/2018.
 */

class SearchCursorAdapter(context: Context?, c: Cursor? = null, flags: Int = 0, private val itemBgColor: Int?) : CursorAdapter(context, c, flags) {

    private val glide: RequestManager = Glide.with(context)

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.item_search_suggestion, parent, false)

        loadContent(view)

        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        loadContent(view)
    }

    private fun loadContent(view: View) {
        val url = cursor.getString(1)
        val ivThumbnail = view.findViewById<ImageView>(R.id.iv_thumbnail)

        if (itemBgColor != null) {
            ivThumbnail.setBackgroundColor(itemBgColor)
        }

        XkcdGlideUtils.load(glide, url, ivThumbnail)

        (view.findViewById<View>(R.id.tv_xkcd_title) as TextView).text = ivThumbnail.context.resources.getString(R.string.item_search_title,
                cursor.getString(3),
                cursor.getString(2))
    }
}
