package com.neuandroid.xkcd;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class ImageDetailPageActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        String url = getIntent().getStringExtra("URL");

        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        Glide.with(this).load(url).into(photoView);
    }
}
