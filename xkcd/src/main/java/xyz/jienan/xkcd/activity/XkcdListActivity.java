package xyz.jienan.xkcd.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import java.util.HashMap;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.XkcdPic_;
import xyz.jienan.xkcd.network.NetworkService;
import xyz.jienan.xkcd.ui.RecyclerViewFastScroller;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;
import static xyz.jienan.xkcd.network.NetworkService.XKCD_BROWSE_LIST;

/**
 * Created by jienanzhang on 22/03/2018.
 */

public class XkcdListActivity extends BaseActivity {

    private static final int INVALID_ID = 0;
    private final static int COUNT_IN_ADV = 10;
    private GridAdapter mAdapter;
    private Box<XkcdPic> box;
    private RecyclerView rvList;
    private RecyclerViewFastScroller scroller;
    private StaggeredGridLayoutManager sglm;
    private int spanCount = 2;
    private boolean loadingMore = false;
    private boolean inRequest = false;
    private RequestManager glide;
    private int latestIndex;
    private RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {

        private static final int FLING_JUMP_LOW_THRESHOLD = 80;
        private static final int FLING_JUMP_HIGH_THRESHOLD = 120;

        private boolean dragging = false;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            dragging = newState == SCROLL_STATE_DRAGGING;
            if (glide.isPaused()) {
                if (newState == SCROLL_STATE_DRAGGING || newState == SCROLL_STATE_IDLE) {
                    // user is touchy or the scroll finished, show images
                    glide.resumeRequests();
                } // settling means the user let the screen go, but it can still be flinging
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = sglm.getChildCount();
            int[] firstVisibileItemPositions = new int[spanCount];
            firstVisibileItemPositions = sglm.findFirstVisibleItemPositions(firstVisibileItemPositions);
//            Timber.v("onScrolled -- \n" +
//                    "visible items: %d\n" +
//                    "adapter items: %d\n" +
//                    "first visible item left panel: %d\n" +
//                    "first visible item right paned: %d",
//                    visibleItemCount, mAdapter.getItemCount(), firstVisibileItemPositions[0], firstVisibileItemPositions[1]);
            if (firstVisibileItemPositions[1] + visibleItemCount >= mAdapter.getItemCount() - COUNT_IN_ADV
                    && !loadingMore
                    && !lastItemReached()) {
                loadingMore = true;
                loadList(mAdapter.getItemCount() + 1);
            }
            if (!dragging) {
                int currentSpeed = Math.abs(dy);
                boolean paused = glide.isPaused();
                if (paused && currentSpeed < FLING_JUMP_LOW_THRESHOLD) {
                    glide.resumeRequests();
                } else if (!paused && FLING_JUMP_HIGH_THRESHOLD < currentSpeed) {
                    glide.pauseRequests();
                }
            }
        }
    };

    //TODO  skip query if in Box
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        rvList = findViewById(R.id.rv_list);
        scroller = findViewById(R.id.rv_scroller);
        scroller.setRecyclerView(rvList);
        scroller.setViewsToUse(R.layout.rv_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        box = ((XkcdApplication) getApplication()).getBoxStore().boxFor(XkcdPic.class);
        mAdapter = new GridAdapter(this);
        rvList.setAdapter(mAdapter);
        rvList.setHasFixedSize(true);
        sglm = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        rvList.setLayoutManager(sglm);
        rvList.addOnScrollListener(rvScrollListener);
        glide = Glide.with(this);
        latestIndex = PreferenceManager.getDefaultSharedPreferences(this).getInt(XKCD_LATEST_INDEX, INVALID_ID);
        loadList(1);
    }

    @Override
    protected void onDestroy() {
        rvList.removeOnScrollListener(rvScrollListener);
        super.onDestroy();
    }

    private boolean lastItemReached() {
        if (mAdapter.getPics() != null) {
            List<XkcdPic> pics = mAdapter.getPics();
            XkcdPic lastPic = pics.get(pics.size() - 1);
            return lastPic.num >= latestIndex;
        }
        return false;
    }

    private void loadList(final int start) {
        Query<XkcdPic> query = box.query().between(XkcdPic_.num, start, start + 399).build();
        query.subscribe().on(AndroidScheduler.mainThread()).observer(new DataObserver<List<XkcdPic>>() {
            @Override
            public void onData(List<XkcdPic> data) {
                int dataSize = data.size();
                Timber.d("Load xkcd list request, start from: %d, the response items: %d", start, dataSize);
                if ((start != 401 && dataSize != 400) || (start == 401 && dataSize != 399)) {
                    if (inRequest) {
                        return;
                    }
                    NetworkService.getXkcdAPI().getXkcdList(XKCD_BROWSE_LIST, start, 0, 400)
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<List<XkcdPic>>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    inRequest = true;
                                }

                                @Override
                                public void onNext(List<XkcdPic> xkcdPics) {
                                    mAdapter.appendList(xkcdPics, true);
                                    loadingMore = false;
                                    inRequest = false;
                                }

                                @Override
                                public void onError(Throwable e) {
                                    inRequest = false;
                                }

                                @Override
                                public void onComplete() {
                                    inRequest = false;
                                }
                            });
                } else {
                    mAdapter.appendList(data, false);
                    loadingMore = false;
                }
            }
        });

    }

    private class GridAdapter extends RecyclerView.Adapter<GridAdapter.XkcdViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

        private Context mContext;

        private List<XkcdPic> pics = new ArrayList<>();

        public GridAdapter(Context context) {
            mContext = context;
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

        @SuppressLint("CheckResult")
        public void appendList(final List<XkcdPic> xkcdPics, boolean checkDB) {
            if (checkDB) {
                final Query<XkcdPic> query = box.query().between(XkcdPic_.num, xkcdPics.get(0).num, xkcdPics.get(xkcdPics.size() - 1).num).build();
                final List<XkcdPic> list = query.find();
                final HashMap<Long, XkcdPic> map = new HashMap<Long, XkcdPic>();
                for (XkcdPic xkcdPic : list) {
                    map.put(xkcdPic.num, xkcdPic);
                }
                Observable.fromArray(xkcdPics.toArray(new XkcdPic[xkcdPics.size()]))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(new Function<XkcdPic, XkcdPic>() {
                            @Override
                            public XkcdPic apply(XkcdPic xkcdPic) throws Exception {
                                XkcdPic pic = map.get(xkcdPic.num);
                                if (pic != null) {
                                    xkcdPic.isFavorite = pic.isFavorite;
                                    xkcdPic.hasThumbed = pic.hasThumbed;
                                }
                                if (!pics.contains(xkcdPic)) {
                                    pics.add(xkcdPic);
                                }
                                return xkcdPic;
                            }
                        }).toList().observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<XkcdPic>>() {
                            @Override
                            public void accept(List<XkcdPic> xkcdPics) throws Exception {
                                box.put(xkcdPics);
                                notifyDataSetChanged();
                                scroller.setVisibility(pics.isEmpty() ? View.GONE : View.VISIBLE);
                            }
                        });
            } else {
                for (XkcdPic pic : xkcdPics) {
                    if (!pics.contains(pic)) {
                        pics.add(pic);
                    }
                }
                notifyDataSetChanged();
                scroller.setVisibility(pics.isEmpty() ? View.GONE : View.VISIBLE);
            }
        }

        public List<XkcdPic> getPics() {
            return pics;
        }

        @Override
        public String getTextToShowInBubble(int pos) {
            return String.valueOf(pos + 1);
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
                    itemXkcdImageNum.setBackground(getResources().getDrawable(R.drawable.ic_heart_on));
                } else {
                    itemXkcdImageNum.setBackground(getResources().getDrawable(R.drawable.item_num_bg));
                }
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
