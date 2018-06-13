package xyz.jienan.xkcd.model;

import android.text.TextUtils;

import org.jsoup.nodes.Element;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import xyz.jienan.xkcd.base.network.NetworkService;
import xyz.jienan.xkcd.base.network.WhatIfAPI;
import xyz.jienan.xkcd.model.persist.BoxManager;
import xyz.jienan.xkcd.model.util.WhatIfArticleUtil;

import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_TOP;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_TOP_SORT_BY_THUMB_UP;

public class WhatIfModel {

    private static WhatIfModel whatIfModel;

    private final WhatIfAPI whatIfAPI = NetworkService.getWhatIfAPI();

    private final BoxManager boxManager = BoxManager.getInstance();

    private final PublishSubject<WhatIfArticle> picsPipeline = PublishSubject.create();

    private WhatIfModel() {
        // no public constructor
    }

    public static WhatIfModel getInstance() {
        if (whatIfModel == null) {
            whatIfModel = new WhatIfModel();
        }
        return whatIfModel;
    }

    public void push(WhatIfArticle article) {
        picsPipeline.onNext(article);
    }

    public Observable<WhatIfArticle> observe() {
        return picsPipeline;
    }

    public Single<List<WhatIfArticle>> loadAllWhatIf() {
        return whatIfAPI.getArchive()
                .subscribeOn(Schedulers.io())
                .singleOrError()
                .map(WhatIfArticleUtil::getArticlesFromArchive)
                .map(boxManager::updateAndSaveWhatIf);
    }

    public Single<WhatIfArticle> loadLatest() {
        return loadAllWhatIf()
                .map(articleList -> articleList.get(articleList.size() - 1));
    }

    public Single<WhatIfArticle> loadArticle(long id) {
        return loadArticleContentFromDB(id)
                .switchIfEmpty(loadArticleFromAPI(id))
                .singleOrError()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public List<WhatIfArticle> loadArticlesFromDB() {
        return boxManager.getWhatIfArchive();
    }

    public WhatIfArticle loadArticleFromDB(long id) {
        return boxManager.getWhatIf(id);
    }

    public Single<List<WhatIfArticle>> searchWhatIf(String query) {
        return Observable.just(boxManager.searchWhatIf(query))
                .singleOrError();
    }

    public Observable<WhatIfArticle> fav(long index, boolean isFav) {
        return Observable.just(boxManager.favWhatIf(index, isFav));
    }

    private Observable<WhatIfArticle> loadArticleFromAPI(long id) {
        return whatIfAPI.getArticle(id)
                .subscribeOn(Schedulers.io())
                .map(WhatIfArticleUtil::getArticleFromHtml)
                .map(Element::html)
                .map(content -> boxManager.updateAndSaveWhatIf(id, content));
    }

    private Observable<WhatIfArticle> loadArticleContentFromDB(long id) {
        WhatIfArticle article = boxManager.getWhatIf(id);
        return (article == null || TextUtils.isEmpty(article.content)) ? Observable.empty() : Observable.just(article);
    }

    public List<WhatIfArticle> getFavWhatIf() {
        return boxManager.getFavWhatIf();
    }

    public Observable<List<WhatIfArticle>> getThumbUpList() {
//        return xkcdApi.getTopXkcds(XKCD_TOP, XKCD_TOP_SORT_BY_THUMB_UP)
//                .subscribeOn(Schedulers.io())
//                .map(boxManager::updateAndSave);
        return Observable.empty();
    }
}
