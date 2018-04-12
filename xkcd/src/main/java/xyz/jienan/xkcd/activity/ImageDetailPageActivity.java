package xyz.jienan.xkcd.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.piasy.biv.view.BigImageView;

import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.XkcdSideloadUtils;
import xyz.jienan.xkcd.network.NetworkService;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class ImageDetailPageActivity extends Activity {
    private PhotoView photoView;
    private BigImageView bigImageView;
    private ProgressBar pbLoading;
    private int index;
    private Box<XkcdPic> box;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        box = ((XkcdApplication) getApplication()).getBoxStore().boxFor(XkcdPic.class);
        String url = getIntent().getStringExtra("URL");
        index = (int) getIntent().getLongExtra("ID", 0L);
        photoView = findViewById(R.id.photo_view);
        bigImageView = findViewById(R.id.big_image_view);
        pbLoading = findViewById(R.id.pb_loading);
        photoView.setMaximumScale(10);
        if (!TextUtils.isEmpty(url)) {
            renderPic(url);
        } else if (index != 0) {
            requestImage();
        } else {
            Timber.e("No valid info for detail page");
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    protected void onDestroy() {
        if (photoView.getVisibility() == View.VISIBLE) {
            Glide.clear(photoView);
        }
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void renderPic(String url) {
        if (XkcdSideloadUtils.useLargeImageView(index)) {
            bigImageView.showImage(Uri.parse(url));
            bigImageView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
        } else {
            Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    pbLoading.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    pbLoading.setVisibility(View.GONE);
                    return false;
                }
            }).into(photoView);
            bigImageView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
        }
        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageDetailPageActivity.this.finish();
                ImageDetailPageActivity.this.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        };
        Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                photoView.setOnClickListener(listener);
                bigImageView.setOnClickListener(listener);
            }
        });
    }

    private void requestImage() {
        Observable<XkcdPic> xkcdPicObservable = NetworkService.getXkcdAPI().getComics(String.valueOf(index));
        xkcdPicObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<XkcdPic>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
                pbLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(XkcdPic resXkcdPic) {
                XkcdPic xkcdPic = box.get(resXkcdPic.num);
                if (xkcdPic != null) {
                    resXkcdPic.isFavorite = xkcdPic.isFavorite;
                    resXkcdPic.hasThumbed = xkcdPic.hasThumbed;
                }
                box.put(resXkcdPic);
                renderPic(resXkcdPic.getTargetImg());
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
