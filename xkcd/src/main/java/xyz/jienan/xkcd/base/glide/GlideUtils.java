package xyz.jienan.xkcd.base.glide;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
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
                                && XkcdSideloadUtils.isSpecialComics(pic)) {
                            load(glide, pic, XkcdSideloadUtils.getPicFromXkcd(pic).getImg(), imageView);
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
        glide.load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
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
}
