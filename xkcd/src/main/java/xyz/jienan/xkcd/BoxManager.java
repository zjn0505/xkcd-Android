package xyz.jienan.xkcd;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class BoxManager {

    private Box<XkcdPic> box;

    public BoxManager() {
        box = XkcdApplication.getInstance().getBoxStore().boxFor(XkcdPic.class);
    }

    @Nullable
    public XkcdPic getXkcd(long index) {
        return box.get(index);
    }

    public void saveXkcd(XkcdPic xkcdPic) {
        box.put(xkcdPic);
    }

    @Nullable
    public XkcdPic like(long index) {
        final XkcdPic xkcdPic = box.get(index);
        if (xkcdPic != null) {
            xkcdPic.hasThumbed = true;
            box.put(xkcdPic);
        }
        return xkcdPic;
    }

    public XkcdPic fav(long index, boolean isFav) {
        final XkcdPic xkcdPic = box.get(index);
        if (xkcdPic != null) {
            xkcdPic.isFavorite = isFav;
            box.put(xkcdPic);
        }
        return xkcdPic;
    }

    @NonNull
    public List<XkcdPic> getXkcdInRange(long start, long end) {
        final Query<XkcdPic> query = box.query().between(XkcdPic_.num, start, end).build();
        return query.find();
    }

    @NonNull
    public List<XkcdPic> getValidXkcdInRange(long start, long end) {
        final Query<XkcdPic> query = box.query()
                .between(XkcdPic_.num, start, end)
                .and().greater(XkcdPic_.width, 0).build();
        return query.find();
    }

    @NonNull
    public List<XkcdPic> getFavXkcd() {
        final Query<XkcdPic> queryFav = box.query().equal(XkcdPic_.isFavorite, true).build();
        return queryFav.find();
    }

    @NonNull
    public List<XkcdPic> updateAndSave(@NonNull List<XkcdPic> xkcdPics) {
        for (XkcdPic pic : xkcdPics) {
            XkcdPic xkcdPicInBox = box.get(pic.num);
            if (xkcdPicInBox != null) {
                pic.isFavorite = xkcdPicInBox.isFavorite;
                pic.hasThumbed = xkcdPicInBox.hasThumbed;
            }
        }
        box.put(xkcdPics);
        return xkcdPics;
    }

    @NonNull
    public XkcdPic updateAndSave(@NonNull XkcdPic xkcdPic) {
        return updateAndSave(Collections.singletonList(xkcdPic)).get(0);
    }
}
