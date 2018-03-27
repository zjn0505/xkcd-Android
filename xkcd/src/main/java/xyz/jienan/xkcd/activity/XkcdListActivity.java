package xyz.jienan.xkcd.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.network.NetworkService;

import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.network.NetworkService.XKCD_BROWSE_LIST;

/**
 * Created by jienanzhang on 22/03/2018.
 */

public class XkcdListActivity extends BaseActivity {

    private GridAdapter mAdapter;
    private List<XkcdPic> pics = new ArrayList<>();
    private Box<XkcdPic> box;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView rvList = new RecyclerView(this);
        setContentView(rvList);
        box = ((XkcdApplication) getApplication()).getBoxStore().boxFor(XkcdPic.class);
        mAdapter = new GridAdapter(this);
        rvList.setAdapter(mAdapter);
        rvList.setHasFixedSize(true);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvList.setLayoutManager(sglm);
        loadList();
    }

    private void loadList() {
        NetworkService.getXkcdAPI().getXkcdList(XKCD_BROWSE_LIST, 1, 0, 400)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<XkcdPic>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<XkcdPic> xkcdPics) {
                        box.put(xkcdPics);
                        pics.addAll(xkcdPics);
                        mAdapter.appendList(xkcdPics);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private class GridAdapter extends RecyclerView.Adapter<GridAdapter.XkcdViewHolder> {

        private Context mContext;
        private List<XkcdPic> pics = new ArrayList<>();
        private RequestManager glide;


        public GridAdapter(Context context) {
            mContext = context;
            glide = Glide.with(mContext);
        }

        @NonNull
        @Override
        public XkcdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_xkcd_list,parent, false);
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

        public void appendList(List<XkcdPic> xkcdPics) {
            pics.addAll(xkcdPics);
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
                        (PercentFrameLayout.LayoutParams)itemXkcdImageView.getLayoutParams();
                PercentLayoutHelper.PercentLayoutInfo info = layoutParams.getPercentLayoutInfo();
                int width = pic.width;
                int height = pic.height;

                info.aspectRatio = ((float)width) / height;
                layoutParams.height = 0;
                itemXkcdImageView.setLayoutParams(layoutParams);
                glide.load(pic.getTargetImg()).asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).fitCenter().into(itemXkcdImageView);
                itemXkcdImageNum.setText(String.valueOf(pic.num));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(XKCD_INDEX_ON_NEW_INTENT, (int) pic.num);
                        startActivity(intent);
                    }
                });
            }
        }
    }

}
