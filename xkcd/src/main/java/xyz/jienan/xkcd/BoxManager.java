package xyz.jienan.xkcd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class BoxManager {

    private Box<XkcdPic> box;

    public BoxManager() {
        box = XkcdApplication.getInstance().getBoxStore().boxFor(XkcdPic.class);
    }

    public List<XkcdPic> getXkcdInRange(long start, long end) {
        Query<XkcdPic> query = box.query().between(XkcdPic_.num, start, end).build();
        return query.find();
    }

    public List<XkcdPic> getFavXkcd() {
        final Query<XkcdPic> queryFav = box.query().equal(XkcdPic_.isFavorite, true).build();
        return queryFav.find();
    }

    public List<XkcdPic> updateAndSave(List<XkcdPic> xkcdPics) {
        for (XkcdPic pic : xkcdPics) {
            XkcdPic xkcdPic = box.get(pic.num);
            if (xkcdPic != null) {
                pic.isFavorite = xkcdPic.isFavorite;
                pic.hasThumbed = xkcdPic.hasThumbed;
            }
        }
        box.put(xkcdPics);
        return xkcdPics;
    }
}
