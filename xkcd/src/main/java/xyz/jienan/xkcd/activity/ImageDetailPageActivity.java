package xyz.jienan.xkcd.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.piasy.biv.view.BigImageView;

import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdSideloadUtils;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class ImageDetailPageActivity extends Activity {
    private PhotoView photoView;
    private BigImageView bigImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        String url = getIntent().getStringExtra("URL");
        int index = (int) getIntent().getLongExtra("ID", 0L);
        photoView = findViewById(R.id.photo_view);
        bigImageView = findViewById(R.id.big_image_view);
        photoView.setMaximumScale(10);
        if (XkcdSideloadUtils.useLargeImageView(index)) {
            bigImageView.showImage(Uri.parse(url));
            bigImageView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
        } else {
            Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
            bigImageView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        };
        photoView.setOnClickListener(listener);
        bigImageView.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        if (photoView.getVisibility() == View.VISIBLE)
            Glide.clear(photoView);
        super.onDestroy();
    }
}
