package xyz.jienan.xkcd.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_what_if_list.view.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.WhatIfArticle

internal class WhatIfListAdapter :
        RecyclerView.Adapter<WhatIfListAdapter.WhatIfViewHolder>(),
        RecyclerViewFastScroller.BubbleTextGetter {

    private lateinit var glide: RequestManager

    var pauseLoading: Boolean
        set(pauseLoading) {
            if (pauseLoading) glide.pauseRequests() else glide.resumeRequests()
        }
        get() = glide.isPaused

    var articles: List<WhatIfArticle>? = listOf()
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhatIfViewHolder {
        glide = Glide.with(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_what_if_list, parent, false)
        return WhatIfViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhatIfViewHolder, position: Int) {
        holder.bind(articles!![holder.adapterPosition])
    }

    override fun getItemCount(): Int = articles?.size ?: 0

    override fun getTextToShowInBubble(pos: Int): String {
        return if (articles != null && articles!![pos].num > 0) {
            articles!![pos].num.toString()
        } else {
            (pos + 1).toString()
        }
    }

    fun updateData(articles: List<WhatIfArticle>) {
        this.articles = articles
        notifyDataSetChanged()
    }

    fun getArticle(position: Int): WhatIfArticle? = articles?.get(position)

    internal inner class WhatIfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(article: WhatIfArticle) {
            glide.load(article.featureImg)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.HIGH)
                    .error(R.drawable.ic_megan)
                    .fitCenter()
                    .into(itemView.itemWhatIfImageView!!)
            itemView.itemWhatIfTitle?.text = itemView.resources.getString(R.string.item_what_if_list_title, article.num, article.title)
            itemView.itemWhatIfFav?.visibility = if (article.isFavorite) View.VISIBLE else View.INVISIBLE
        }
    }
}