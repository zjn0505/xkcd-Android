package xyz.jienan.xkcd.base.glide;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils;

public class GlideUtils {

    public static void load(RequestManager glide, XkcdPic pic, @NonNull String url, ImageView imageView) {
        glide.load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.HIGH)
                .fitCenter()
                .dontTransform()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        if (model.startsWith("https")) {
                            load(glide, pic, model.replaceFirst("https", "http"), imageView);
                            return true;
                        } else if (model.replaceFirst("http", "https").equals(pic.getTargetImg())
                                && XkcdSideloadUtils.INSTANCE.isSpecialComics(pic)) {
                            load(glide, pic, XkcdSideloadUtils.INSTANCE.getPicFromXkcd(pic).getImg(), imageView);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model,
                                                   Target<Bitmap> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }

    public static void load(RequestManager glide, @NonNull String url, ImageView imageView) {
        glide.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target, boolean isFirstResource) {
                        if (model.startsWith("https")) {
                            load(glide, model.replaceFirst("https", "http"), imageView);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                                                   String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        return false;
                    }
                }).into(imageView);
    }

    public static void loadGif(RequestManager glide, @NonNull String url, Target<GifDrawable> target) {
        glide.load(url)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GifDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GifDrawable> target, boolean isFirstResource) {
                        if (model.startsWith("https")) {
                            loadGif(glide, model.replaceFirst("https", "http"), target);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource,
                                                   String model,
                                                   Target<GifDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        return false;
                    }
                })
                .into(target);
    }
}
