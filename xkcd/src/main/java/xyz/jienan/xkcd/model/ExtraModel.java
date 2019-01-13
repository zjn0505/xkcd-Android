package xyz.jienan.xkcd.model;


import android.text.TextUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;
import xyz.jienan.xkcd.model.persist.BoxManager;
import xyz.jienan.xkcd.model.util.ExtraHtmlUtil;
import xyz.jienan.xkcd.model.util.XkcdExplainUtil;

public class ExtraModel {

    private static ExtraModel extraModel;

    private final BoxManager boxManager = BoxManager.getInstance();

    private ExtraModel() {
        // no public constructor
    }

    public static ExtraModel getInstance() {
        if (extraModel == null) {
            extraModel = new ExtraModel();
        }
        return extraModel;
    }

    public ExtraComics getExtra(int index) {
        return boxManager.getExtra(index);
    }

    public void update(List<ExtraComics> extraComics) {
        boxManager.saveExtras(extraComics);
    }

    public List<ExtraComics> getAll() {
        return boxManager.getExtraList();
    }

    public Observable<String> loadExplain(String url) {
        String explainFromDB = boxManager.loadExtraExplain(url);
        if (!TextUtils.isEmpty(explainFromDB)) {
            return Observable.just(explainFromDB);
        }

        return NetworkService.getXkcdAPI().getExplain(url).subscribeOn(Schedulers.io())
                .map(responseBody -> XkcdExplainUtil.getExplainFromHtml(responseBody, url));
    }

    public void saveExtraWithExplain(String url, String explainContent) {
        boxManager.updateExtra(url, explainContent);
    }

    public Observable<String> parseContentFromUrl(String url) {
        return ExtraHtmlUtil.getContentFromUrl(url)
                .observeOn(AndroidSchedulers.mainThread());
    }
}
