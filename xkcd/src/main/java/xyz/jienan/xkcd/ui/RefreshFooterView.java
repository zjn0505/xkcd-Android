package xyz.jienan.xkcd.ui;

import android.content.Context;
import android.util.AttributeSet;

import me.dkzwm.widget.srl.extra.footer.ClassicFooter;

public class RefreshFooterView extends ClassicFooter {
    public RefreshFooterView(Context context) {
        super(context);
    }

    public RefreshFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshFooterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTextSize(float size) {
        mTitleTextView.setTextSize(size);
    }
}
