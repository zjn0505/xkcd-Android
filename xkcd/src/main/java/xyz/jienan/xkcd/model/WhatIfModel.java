package xyz.jienan.xkcd.model;

import org.jsoup.nodes.Element;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;
import xyz.jienan.xkcd.base.network.WhatIfAPI;
import xyz.jienan.xkcd.model.persist.BoxManager;
import xyz.jienan.xkcd.model.util.WhatIfArticleUtil;

public class WhatIfModel {

    private static WhatIfModel whatIfModel;

    private final WhatIfAPI whatIfAPI = NetworkService.getWhatIfAPI();

    private final BoxManager boxManager = BoxManager.getInstance();

    private WhatIfModel() {
        // no public constructor
    }

    public static WhatIfModel getInstance() {
        if (whatIfModel == null) {
            whatIfModel = new WhatIfModel();
        }
        return whatIfModel;
    }

    public Single<WhatIfArticle> loadLatest() {
        return whatIfAPI.getArchive()
                .subscribeOn(Schedulers.io())
                .singleOrError()
                .map(WhatIfArticleUtil::getArticlesFromArchive)
                .map(boxManager::updateAndSaveWhatIf)
                .map(articleList -> articleList.get(articleList.size() - 1));
    }

    public Single<String> loadArticle(long id) {
        return whatIfAPI.getArticle(id)
                .subscribeOn(Schedulers.io())
                .singleOrError()
                .map(WhatIfArticleUtil::getArticleFromHtml)
                .map(Element::html)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<WhatIfArticle>> searchWhatIf(String query) {
        return Observable.just(boxManager.searchWhatIf(query))
                .subscribeOn(Schedulers.io())
                .singleOrError();
    }
}
