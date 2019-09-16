package xyz.jienan.xkcd.base.glide;

import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.request.target.Target;

import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.ui.IProgressbar;

import static android.view.View.VISIBLE;

public class MyProgressTarget<Z> extends ProgressTarget<String, Z> {
    private final IProgressbar progressbar;
    private final ImageView image;

    public MyProgressTarget(Target<Z> target, IProgressbar progress, ImageView image) {
        super(target);
        this.progressbar = progress;
        this.image = image;
    }

    @Override
    public float getGranualityPercentage() {
        return 0.1f; // this matches the format string for #text below
    }

    @Override
    public void onDownloadStart() {

    }

    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onDownloadFinish() {

    }

    @Override
    protected void onConnecting() {
        progressbar.setProgress(1);
        progressbar.setVisibility(VISIBLE);
        image.setImageLevel(0);
    }

    @Override
    protected void onDownloading(long bytesRead, long expectedLength) {
        int progress = (int) (100 * bytesRead / expectedLength);
        progress = progress <= 0 ? 1 : progress;
        progressbar.setProgress(progress);
        if (progressbar.getAnimation() == null || !progressbar.getAnimation().hasStarted()) {
            progressbar.startAnimation(AnimationUtils.loadAnimation(image.getContext(), R.anim.rotate));
        }
        image.setImageLevel((int) (10000 * bytesRead / expectedLength));
    }

    @Override
    protected void onDownloaded() {
        image.setImageLevel(10000);
    }

    @Override
    protected void onDelivered() {
        progressbar.setVisibility(View.INVISIBLE);
        progressbar.clearAnimation();
        image.setImageLevel(0); // reset ImageView default
    }
}
