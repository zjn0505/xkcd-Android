package xyz.jienan.xkcd.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
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
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.model.XkcdModel;
import xyz.jienan.xkcd.model.XkcdPic;

public class XkcdListGridAdapter extends RecyclerView.Adapter<XkcdListGridAdapter.XkcdViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

    private Context mContext;
    private RequestManager glide;
    private List<XkcdPic> pics = new ArrayList<>();
    private XkcdModel xkcdModel = XkcdModel.getInstance();

    public XkcdListGridAdapter(Context context) {
        mContext = context;
        glide = Glide.with(context);
    }

    public RequestManager getGlide() {
        return glide;
    }

    @NonNull
    @Override
    public XkcdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_xkcd_list, parent, false);
        return new XkcdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull XkcdViewHolder holder, int position) {
        XkcdPic pic = pics.get(holder.getAdapterPosition());
        holder.bind(pic);
    }

    @Override
    public int getItemCount() {
        return pics == null ? 0 : pics.size();
    }

    public List<XkcdPic> getPics() {
        return pics;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (pics != null && pics.get(pos) != null && pics.get(pos).num > 0) {
            return String.valueOf(pics.get(pos).num);
        } else {
            return String.valueOf(pos + 1);
        }
    }

    public void updateData(List<XkcdPic> pics) {
        this.pics = pics;
        notifyDataSetChanged();
    }

    public XkcdPic getPic(int position) {
        return pics.get(position);
    }

    class XkcdViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_item_xkcd_list)
        ImageView itemXkcdImageView;
        @BindView(R.id.tv_item_xkcd_num)
        TextView itemXkcdImageNum;

        XkcdViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("CheckResult")
        void bind(final XkcdPic pic) {
            PercentFrameLayout.LayoutParams layoutParams =
                    (PercentFrameLayout.LayoutParams) itemXkcdImageView.getLayoutParams();
            PercentLayoutHelper.PercentLayoutInfo info = layoutParams.getPercentLayoutInfo();
            int width = pic.width;
            int height = pic.height;

            info.aspectRatio = ((float) width) / height;
            layoutParams.height = 0;
            glide.load(pic.getTargetImg())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.HIGH)
                    .fitCenter()
                    .dontTransform()
                    .into(itemXkcdImageView);
            itemXkcdImageNum.setText(String.valueOf(pic.num));
            if (pic.isFavorite) {
                itemXkcdImageNum.setBackground(mContext.getResources().getDrawable(R.drawable.ic_heart_on));
            } else {
                itemXkcdImageNum.setBackground(mContext.getResources().getDrawable(R.drawable.item_num_bg));
            }
            if (width == 0 || height == 0) {
                xkcdModel.loadXkcd(pic.num)
                        .subscribe(xkcdPic -> {
                            if (xkcdPic != null) {
                                xkcdModel.updateSize(xkcdPic.num, xkcdPic.width, xkcdPic.height);
                            }
                        }, e -> Timber.e(e, "reload size error"));
            }
        }
    }
}