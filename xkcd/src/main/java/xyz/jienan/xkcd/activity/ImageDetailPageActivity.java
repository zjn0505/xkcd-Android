package xyz.jienan.xkcd.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import xyz.jienan.xkcd.R;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class ImageDetailPageActivity extends Activity {
    private PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        String url = getIntent().getStringExtra("URL");

        photoView = findViewById(R.id.photo_view);
        photoView.setMaximumScale(5);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
//        Glide.with(this).load(url).override(width,4096).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);
        Glide.with(this).load(url).override(4096,4096).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Glide.clear(photoView);
        super.onDestroy();
    }
}
