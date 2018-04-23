package xyz.jienan.xkcd.ui;

import android.content.Context;
import android.content.Intent;
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

import io.objectbox.Box;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.activity.MainActivity;
import xyz.jienan.xkcd.network.NetworkService;

import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.network.NetworkService.XKCD_BROWSE_LIST;

public class XkcdListGridAdapter extends RecyclerView.Adapter<XkcdListGridAdapter.XkcdViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

    private Context mContext;
    private RequestManager glide;
    private List<XkcdPic> pics = new ArrayList<>();
    private Box<XkcdPic> box;

    public XkcdListGridAdapter(Context context) {
        mContext = context;
        glide = Glide.with(context);
        box = ((XkcdApplication) context.getApplicationContext()).getBoxStore().boxFor(XkcdPic.class);
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

    class XkcdViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemXkcdImageView;
        private TextView itemXkcdImageNum;

        public XkcdViewHolder(View itemView) {
            super(itemView);
            itemXkcdImageView = itemView.findViewById(R.id.iv_item_xkcd_list);
            itemXkcdImageNum = itemView.findViewById(R.id.tv_item_xkcd_num);
        }

        public void bind(final XkcdPic pic) {
            PercentFrameLayout.LayoutParams layoutParams =
                    (PercentFrameLayout.LayoutParams) itemXkcdImageView.getLayoutParams();
            PercentLayoutHelper.PercentLayoutInfo info = layoutParams.getPercentLayoutInfo();
            int width = pic.width;
            int height = pic.height;

            info.aspectRatio = ((float) width) / height;
            layoutParams.height = 0;
            itemXkcdImageView.setLayoutParams(layoutParams);
            glide.load(pic.getTargetImg()).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).priority(Priority.HIGH).fitCenter().into(itemXkcdImageView);
            itemXkcdImageNum.setText(String.valueOf(pic.num));
            if (pic.isFavorite) {
                itemXkcdImageNum.setBackground(mContext.getResources().getDrawable(R.drawable.ic_heart_on));
            } else {
                itemXkcdImageNum.setBackground(mContext.getResources().getDrawable(R.drawable.item_num_bg));
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(XKCD_INDEX_ON_NEW_INTENT, (int) pic.num);
                    mContext.startActivity(intent);
                }
            });
            if (width == 0 || height == 0) {
                NetworkService.getXkcdAPI().getXkcdList(XKCD_BROWSE_LIST, (int) pic.num, 0, 1)
                        .subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Observer<List<XkcdPic>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<XkcdPic> xkcdPics) {
                        if (xkcdPics != null && xkcdPics.size() == 1) {
                            XkcdPic xkcdPic = xkcdPics.get(0);
                            XkcdPic xkcdPicBox = box.get(pic.num);
                            xkcdPicBox.width = xkcdPic.width;
                            xkcdPicBox.height = xkcdPic.height;
                            box.put(xkcdPicBox);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        }
    }
}