package xyz.jienan.xkcd;


import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;

import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_BROWSE_LIST;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_TOP;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_TOP_SORT_BY_THUMB_UP;

public class XkcdDAO {

    private BoxManager boxManager;

    private final static int SLICE = 400;

    public XkcdDAO() {
        boxManager = new BoxManager();
    }

    public Observable<XkcdPic> loadLatest() {
        return NetworkService.getXkcdAPI()
                .getLatest()
                .subscribeOn(Schedulers.io())
                .map(boxManager::updateAndSave);
    }

    public Observable<XkcdPic> loadXkcd(long index) {
        return NetworkService.getXkcdAPI()
                .getXkcdList(XKCD_BROWSE_LIST, (int) index, 0, 1)
                .subscribeOn(Schedulers.io())
                .map(xkcdPics -> xkcdPics.get(0))
                .map(boxManager::updateAndSave);
    }

    /**
     * fast loading all xkcd pics
     * @param latestIndex
     * @return
     */
    public Observable<Boolean> fastLoad(int latestIndex) {
        return Observable.range(0, (latestIndex - 1) / SLICE + 1)
                .subscribeOn(Schedulers.io())
                .map(i -> i * 400 + 1)
                .flatMap(startIndex -> Observable.just(boxManager.getValidXkcdInRange(startIndex, startIndex + SLICE - 1))
                        .filter(xkcdPics -> xkcdPics.size() != SLICE)
                        .filter(xkcdPics -> {
                            if (xkcdPics.isEmpty()) {
                                return true;
                            } else {
                                final int size = xkcdPics.size();
                                if (startIndex <= 404 && startIndex + SLICE > 404 && size != SLICE - 1) {
                                    return true;
                                } else if (startIndex > latestIndex - SLICE + 1 && startIndex + size - 1 != latestIndex) {
                                    return true;
                                }
                            }
                            return false;
                        }).flatMap(ignored -> loadRange(startIndex, SLICE)))
                .map(ignored -> Boolean.TRUE);
    }

    public Observable<List<XkcdPic>> getThumbUpList() {
        return NetworkService.getXkcdAPI()
                .getTopXkcds(XKCD_TOP, XKCD_TOP_SORT_BY_THUMB_UP)
                .subscribeOn(Schedulers.io())
                .map(boxManager::updateAndSave);
    }

    /**
     *
     * @param start
     * @param range
     * @return last index.
     */
    public Observable<List<XkcdPic>> loadRange(long start, long range) {
        return NetworkService.getXkcdAPI()
                .getXkcdList(XKCD_BROWSE_LIST, (int) start, 0, (int) range)
                .subscribeOn(Schedulers.io())
                .map(boxManager::updateAndSave);
    }

    /**
     *
     * @param index
     * @return thumb up count
     */
    public Observable<Long> thumbsUp(long index) {
        return NetworkService.getXkcdAPI()
                .thumbsUp(NetworkService.XKCD_THUMBS_UP, (int) index)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(ignored -> boxManager.like(index))
                .map(boxManager::updateAndSave)
                .map(xkcdPic -> xkcdPic.thumbCount);
    }

    public Single<Boolean> validateXkcdList(List<XkcdPic> xkcdList) {
        return Observable.fromIterable(xkcdList)
                .subscribeOn(Schedulers.io())
                .filter(xkcdPic -> xkcdPic == null || xkcdPic.width == 0 || xkcdPic.height == 0)
                .toSortedList()
                .flatMap(xkcdPics -> {
                    if (xkcdPics.isEmpty()) {
                        return Single.just(0);
                    } else {
                        return loadRange(xkcdPics.get(0).num,
                                xkcdPics.get(xkcdPics.size() - 1).num).singleOrError();
                    }
                }).map(ignore -> Boolean.TRUE);
    }

    public List<XkcdPic> getFavXkcd() {
        return boxManager.getFavXkcd();
    }

    public List<XkcdPic> loadXkcdFromDB(long start, long end) {
        return boxManager.getXkcdInRange(start, end);
    }

    public Observable<XkcdPic> fav(long index, boolean isFav) {
        XkcdPic xkcdPicInBox = boxManager.fav(index, isFav);
        if (xkcdPicInBox.width == 0 || xkcdPicInBox.height == 0) {
            return loadXkcd(index);
        } else {
            return Observable.just(xkcdPicInBox);
        }
    }
}
