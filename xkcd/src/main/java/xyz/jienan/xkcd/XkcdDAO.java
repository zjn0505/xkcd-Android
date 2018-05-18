package xyz.jienan.xkcd;


import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;

import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_BROWSE_LIST;

public class XkcdDAO {

    private BoxManager boxManager;

    private final static int SLICE = 400;

    public XkcdDAO(BoxManager boxManager) {
        this.boxManager = boxManager;
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
    public Observable<Long> fastLoad(int latestIndex) {
        return Observable.range(0, (latestIndex - 1) / SLICE + 1)
                .subscribeOn(Schedulers.io())
                .map(i -> i * 400 + 1)
                .map(startIndex -> boxManager.getValidXkcdInRange(startIndex, startIndex + SLICE - 1))
                .filter(xkcdPics -> xkcdPics.size() != SLICE)
                .filter(xkcdPics -> {
                    if (xkcdPics.isEmpty()) {
                        return true;
                    } else {
                        final long startIndex = xkcdPics.get(0).num;
                        final int size = xkcdPics.size();
                        if (startIndex <= 404 && startIndex + SLICE > 404 && size != SLICE - 1) {
                            return true;
                        } else if (startIndex > latestIndex - SLICE + 1 && startIndex + size - 1 != latestIndex) {
                            return true;
                        }
                    }
                    return false;
                }).map(xkcdPics -> xkcdPics.get(0).num)
                .flatMap(startIndex -> loadRange(startIndex, SLICE));
    }


    /**
     *
     * @param start
     * @param range
     * @return last index.
     */
    public Observable<Long> loadRange(long start, long range) {
        return NetworkService.getXkcdAPI()
                .getXkcdList(XKCD_BROWSE_LIST, (int) start, 0, (int) range)
                .subscribeOn(Schedulers.io())
                .map(boxManager::updateAndSave)
                .map(list -> list.get(list.size() - 1).num);
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
                .map(boxManager::updateAndSave)
                .map(xkcdPic -> xkcdPic.thumbCount);
    }
}
