package xyz.jienan.xkcd.ui;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

public class AnimUtils {

    public static void vectorAnim(ImageView view, int animId, int fallbackResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vectorAnim(view, animId);
        } else {
            view.setImageResource(fallbackResId);
        }
    }

    public static void vectorAnim(ImageView view, int animId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setImageResource(animId);
            Drawable drawable = view.getDrawable();
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        }
    }
}
