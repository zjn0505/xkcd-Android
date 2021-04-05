package xyz.jienan.xkcd.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.percentlayout.widget.PercentFrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.item_xkcd_list.view.*
import timber.log.Timber
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.glide.XkcdGlideUtils
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic

internal class XkcdListGridAdapter : ListBaseAdapter<XkcdListGridAdapter.XkcdViewHolder>(), RecyclerViewFastScroller.BubbleTextGetter {

    private lateinit var glide: RequestManager

    var pics: List<XkcdPic>? = listOf()
        private set

    override var pauseLoading: Boolean
        set(pauseLoading) {
            if (pauseLoading) glide.pauseRequests() else glide.resumeRequests()
        }
        get() = glide.isPaused

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XkcdViewHolder {
        glide = Glide.with(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_xkcd_list, parent, false)
        return XkcdViewHolder(view)
    }

    override fun onBindViewHolder(holder: XkcdViewHolder, position: Int) {
        holder.bind(pics!![holder.bindingAdapterPosition])
    }

    override fun getItemCount(): Int = pics?.size ?: 0

    override fun getTextToShowInBubble(pos: Int): String {
        return if (pics != null && pics!![pos].num > 0) {
            pics!![pos].num.toString()
        } else {
            (pos + 1).toString()
        }
    }

    fun updateData(pics: List<XkcdPic>) {
        this.pics = pics
        notifyDataSetChanged()
    }

    fun getPic(position: Int): XkcdPic? = pics?.get(position)

    internal inner class XkcdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("CheckResult")
        fun bind(pic: XkcdPic) {
            val layoutParams = itemView.itemXkcdImageView!!.layoutParams as PercentFrameLayout.LayoutParams
            val info = layoutParams.percentLayoutInfo
            val width = pic.width
            val height = pic.height

            info.aspectRatio = width.toFloat() / height
            layoutParams.height = 0
            XkcdGlideUtils.load(glide, pic, pic.targetImg, itemView.itemXkcdImageView)
            itemView.itemXkcdImageNum?.text = pic.num.toString()
            itemView.itemXkcdImageNum?.background = if (pic.isFavorite) {
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_heart_on)
            } else {
                ContextCompat.getDrawable(itemView.context, R.drawable.item_num_bg)
            }

            if (width == 0 || height == 0) {
                XkcdModel.loadXkcd(pic.num)
                        .subscribe({ xkcdPic ->
                            if (xkcdPic != null) {
                                XkcdModel.updateSize(xkcdPic.num, xkcdPic.width, xkcdPic.height)
                            }
                        }, { e -> Timber.e(e, "reload size error") })
            }
        }
    }
}