/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.jienan.xkcd.base.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Piasy{github.com/Piasy} on 09/11/2016.
 */

public final class GlideImageLoader implements ImageLoader {
    private final ConcurrentHashMap<Integer, ImageDownloadTarget> mRequestTargetMap
            = new ConcurrentHashMap<>();
    private Context context;

    private GlideImageLoader(Context context) {
        this.context = context;
    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    @Override
    public void loadImage(final int requestId, final Uri uri, final Callback callback) {
        ImageDownloadTarget target = new ImageDownloadTarget(uri.toString()) {

            @Override
            public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                super.onResourceReady(resource, glideAnimation);
                // we don't need delete this image file, so it behaves live cache hit
                callback.onCacheHit(resource);
                callback.onSuccess(resource);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                if (uri.getPath().startsWith("https")) {
                    Glide.with(context).load(Uri.parse(uri.getPath().replaceFirst("https", "http")))
                            .downloadOnly(this);
                    return;
                }
                callback.onFail(new GlideLoaderException(errorDrawable));
            }

            @Override
            public void onProgress(long bytesRead, long expectedLength) {
                int progress = (int) ((float) bytesRead / expectedLength * 100);
                callback.onProgress(progress);
            }

            @Override
            public float getGranualityPercentage() {
                return 0;
            }

            @Override
            public void onDownloadStart() {
                callback.onStart();
            }

            @Override
            public void onProgress(int progress) {
                callback.onProgress(progress);
            }

            @Override
            public void onDownloadFinish() {
                callback.onFinish();
            }
        };
        clearTarget(requestId);
        saveTarget(requestId, target);

        Glide.with(context)
                .load(uri)
                .downloadOnly(target);
    }

    @Override
    public View showThumbnail(BigImageView parent, Uri thumbnail, int scaleType) {
        // not used
        return null;
    }

    private void saveTarget(int requestId, ImageDownloadTarget target) {
        mRequestTargetMap.put(requestId, target);
    }

    private void clearTarget(int requestId) {
        ImageDownloadTarget target = mRequestTargetMap.remove(requestId);
        if (target != null) {
            Glide.clear(target);
        }
    }

    @Override
    public void prefetch(Uri uri) {
        Glide.with(context)
                .load(uri)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {

                    }
                });
    }

    @Override
    public void cancel(int requestId) {
        clearTarget(requestId);
    }
}
