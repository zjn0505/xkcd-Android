package xyz.jienan.xkcd.ui.like;

import androidx.annotation.DrawableRes;

/**
 * Created by Joel on 23/12/2015.
 */
public class Icon {
    private int onIconResourceId;
    private int offIconResourceId;
    private IconType iconType;

    public Icon(@DrawableRes int onIconResourceId, @DrawableRes int offIconResourceId, IconType iconType) {
        this.onIconResourceId = onIconResourceId;
        this.offIconResourceId = offIconResourceId;
        this.iconType = iconType;
    }

    public int getOffIconResourceId() {
        return offIconResourceId;
    }

    public int getOnIconResourceId() {
        return onIconResourceId;
    }

    public IconType getIconType() {
        return iconType;
    }
}