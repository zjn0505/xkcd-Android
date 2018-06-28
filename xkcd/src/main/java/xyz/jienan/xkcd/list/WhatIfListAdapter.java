package xyz.jienan.xkcd.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.model.WhatIfArticle;

public class WhatIfListAdapter extends RecyclerView.Adapter<WhatIfListAdapter.WhatIfViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

    private Context mContext;
    private RequestManager glide;
    private List<WhatIfArticle> articles = new ArrayList<>();

    public WhatIfListAdapter(Context context) {
        mContext = context;
        glide = Glide.with(context);
    }

    public RequestManager getGlide() {
        return glide;
    }

    @NonNull
    @Override
    public WhatIfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_what_if_list, parent, false);
        return new WhatIfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WhatIfViewHolder holder, int position) {
        WhatIfArticle article = articles.get(holder.getAdapterPosition());
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles == null ? 0 : articles.size();
    }

    public List<WhatIfArticle> getArticles() {
        return articles;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (articles != null && articles.get(pos) != null && articles.get(pos).num > 0) {
            return String.valueOf(articles.get(pos).num);
        } else {
            return String.valueOf(pos + 1);
        }
    }

    public void updateData(List<WhatIfArticle> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    class WhatIfViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_item_what_if_list)
        ImageView itemWhatIfImageView;
        @BindView(R.id.tv_item_what_if_title)
        TextView itemWhatIfTitle;
        @BindView(R.id.iv_fav_what_if_list)
        ImageView itemWhatIfFav;

        WhatIfViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("CheckResult")
        void bind(final WhatIfArticle article) {
            glide.load(article.featureImg)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.HIGH)
                    .error(R.drawable.ic_megan)
                    .fitCenter()
                    .into(itemWhatIfImageView);
            itemWhatIfTitle.setText(mContext.getString(R.string.item_what_if_list_title, article.num, article.title));
            if (article.isFavorite) {
                itemWhatIfFav.setVisibility(View.VISIBLE);
            } else {
                itemWhatIfFav.setVisibility(View.INVISIBLE);
            }
        }
    }
}