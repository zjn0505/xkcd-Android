package xyz.jienan.xkcd.model;


import android.text.Html;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils;

/**
 * Created by jienanzhang on 09/07/2017.
 */

@Entity
public class XkcdPic {
    public String year;
    public String month;
    public String day;
    @Id(assignable = true)
    public long num;
    private String alt;
    public boolean large = false;
    public boolean special = false;
    public int width;
    public int height;
    public boolean isFavorite = false;
    public boolean hasThumbed = false;
    public long thumbCount;
    private String title;
    private String img;

    public String getTargetImg() {
        return XkcdSideloadUtils.INSTANCE.sideload(this).img;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return Html.fromHtml(title).toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlt() {
        return Html.fromHtml(alt).toString();
    }

    public XkcdPic clone() {
        XkcdPic clone = new XkcdPic();
        clone.year = year;
        clone.month = month;
        clone.day = day;
        clone.num = num;
        clone.title = title;
        clone.img = img;
        clone.alt = alt;
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof XkcdPic && this.num == ((XkcdPic) obj).num;
    }
}
