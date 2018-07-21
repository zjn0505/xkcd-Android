package xyz.jienan.xkcd.model.persist;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.util.NumberUtils;

import java.util.Collections;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.WhatIfArticle_;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.XkcdPic_;

import static xyz.jienan.xkcd.Const.PREF_WHAT_IF_SEARCH_ALL;
import static xyz.jienan.xkcd.Const.PREF_WHAT_IF_SEARCH_INCLUDE_READ;

public class BoxManager {

    private static BoxManager boxManager;
    private Box<XkcdPic> xkcdBox;
    private Box<WhatIfArticle> whatIfBox;

    private BoxManager() {
        xkcdBox = XkcdApplication.getInstance().getBoxStore().boxFor(XkcdPic.class);
        whatIfBox = XkcdApplication.getInstance().getBoxStore().boxFor(WhatIfArticle.class);
    }

    public static BoxManager getInstance() {
        if (boxManager == null) {
            boxManager = new BoxManager();
        }
        return boxManager;
    }

    /********** xkcd **********/

    @Nullable
    public XkcdPic getXkcd(long index) {
        return xkcdBox.get(index);
    }

    public void saveXkcd(XkcdPic xkcdPic) {
        xkcdBox.put(xkcdPic);
    }

    @Nullable
    public XkcdPic likeXkcd(long index) {
        final XkcdPic xkcdPic = xkcdBox.get(index);
        if (xkcdPic != null) {
            xkcdPic.hasThumbed = true;
            xkcdBox.put(xkcdPic);
        }
        return xkcdPic;
    }

    public XkcdPic favXkcd(long index, boolean isFav) {
        final XkcdPic xkcdPic = xkcdBox.get(index);
        if (xkcdPic != null) {
            xkcdPic.isFavorite = isFav;
            xkcdBox.put(xkcdPic);
        }
        return xkcdPic;
    }

    @NonNull
    public List<XkcdPic> getXkcdInRange(long start, long end) {
        final Query<XkcdPic> query = xkcdBox.query().between(XkcdPic_.num, start, end).build();
        return query.find();
    }

    @NonNull
    public List<XkcdPic> getValidXkcdInRange(long start, long end) {
        final Query<XkcdPic> query = xkcdBox.query()
                .between(XkcdPic_.num, start, end)
                .and().greater(XkcdPic_.width, 0).build();
        return query.find();
    }

    @NonNull
    public List<XkcdPic> getFavXkcd() {
        final Query<XkcdPic> queryFav = xkcdBox.query().equal(XkcdPic_.isFavorite, true).build();
        return queryFav.find();
    }

    @NonNull
    public List<XkcdPic> updateAndSave(@NonNull List<XkcdPic> xkcdPics) {
        for (XkcdPic pic : xkcdPics) {
            XkcdPic xkcdPicInBox = xkcdBox.get(pic.num);
            if (xkcdPicInBox != null) {
                pic.isFavorite = xkcdPicInBox.isFavorite;
                pic.hasThumbed = xkcdPicInBox.hasThumbed;
                if (pic.width == 0 && xkcdPicInBox.width != 0) {
                    pic.width = xkcdPicInBox.width;
                    pic.height = xkcdPicInBox.height;
                }
            }
        }
        xkcdBox.put(xkcdPics);
        return xkcdPics;
    }

    @NonNull
    public XkcdPic updateAndSave(@NonNull XkcdPic xkcdPic) {
        return updateAndSave(Collections.singletonList(xkcdPic)).get(0);
    }

    /********** what if **********/

    @Nullable
    public List<WhatIfArticle> getWhatIfArchive() {
        return whatIfBox.getAll();
    }

    @Nullable
    public WhatIfArticle getWhatIf(long index) {
        return whatIfBox.get(index);
    }

    @NonNull
    public List<WhatIfArticle> updateAndSaveWhatIf(@NonNull List<WhatIfArticle> whatIfArticles) {
        for (WhatIfArticle article : whatIfArticles) {
            WhatIfArticle articleInBox = whatIfBox.get(article.num);
            if (articleInBox != null && articleInBox.content != null) {
                whatIfArticles.set((int) article.num - 1, articleInBox);
            }
        }
        whatIfBox.put(whatIfArticles);
        return whatIfArticles;
    }

    public WhatIfArticle updateAndSaveWhatIf(long index, String content) {
        final WhatIfArticle article = whatIfBox.get(index);
        if (article != null) {
            article.content = content;
            whatIfBox.put(article);
        }
        return article;
    }

    @NonNull
    public List<WhatIfArticle> searchWhatIf(@NonNull String query, String option) {
        QueryBuilder<WhatIfArticle> builder = whatIfBox.query().contains(WhatIfArticle_.title, query);
        if (NumberUtils.isNumeric(query)) {
            builder = builder.or().equal(WhatIfArticle_.num, Long.valueOf(query));
        }
        switch (option) {
            case PREF_WHAT_IF_SEARCH_INCLUDE_READ:
                builder = builder.or()
                        .contains(WhatIfArticle_.content, query)
                        .and()
                        .equal(WhatIfArticle_.hasThumbed, true)
                        .or()
                        .equal(WhatIfArticle_.isFavorite, true);
                break;
            case PREF_WHAT_IF_SEARCH_ALL:
                builder = builder.or()
                        .contains(WhatIfArticle_.content, query);
                break;
        }
        return builder.build().find();
    }

    @Nullable
    public WhatIfArticle likeWhatIf(long index) {
        final WhatIfArticle article = whatIfBox.get(index);
        if (article != null) {
            article.hasThumbed = true;
            whatIfBox.put(article);
        }
        return article;
    }

    public WhatIfArticle favWhatIf(long index, boolean isFav) {
        final WhatIfArticle article = whatIfBox.get(index);
        if (article != null) {
            article.isFavorite = isFav;
            whatIfBox.put(article);
        }
        return article;
    }

    public List<WhatIfArticle> getFavWhatIf() {
        final Query<WhatIfArticle> queryFav = whatIfBox.query().equal(WhatIfArticle_.isFavorite, true).build();
        return queryFav.find();
    }
}
