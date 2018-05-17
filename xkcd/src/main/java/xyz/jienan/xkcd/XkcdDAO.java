package xyz.jienan.xkcd;


import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;

import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_BROWSE_LIST;

public class XkcdDAO {

    private BoxManager boxManager;

    public XkcdDAO(BoxManager boxManager) {
        this.boxManager = boxManager;
    }

    public Observable<Long> loadRange(int start, int range) {
        return NetworkService.getXkcdAPI()
                .getXkcdList(XKCD_BROWSE_LIST, start, 0, range)
                .subscribeOn(Schedulers.io())
                .map(boxManager::updateAndSave)
                .map(list -> list.get(list.size() - 1).num);
    }
}
