package xyz.jienan.xkcd.ui;

import android.content.Context;
import android.util.AttributeSet;

import me.dkzwm.widget.srl.extra.header.ClassicHeader;

public class RefreshHeaderView extends ClassicHeader {
    public RefreshHeaderView(Context context) {
        super(context);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTextSize(float size) {
        mTitleTextView.setTextSize(size);
    }
}
