package xyz.jienan.xkcd.model


import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import xyz.jienan.xkcd.base.network.NetworkService
import xyz.jienan.xkcd.base.network.XKCD_BROWSE_LIST
import xyz.jienan.xkcd.base.network.XKCD_EXPLAIN_URL
import xyz.jienan.xkcd.base.network.XKCD_TOP_SORT_BY_THUMB_UP
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.util.XkcdExplainUtil
import java.util.concurrent.TimeUnit

object XkcdModel {

    var localizedUrl: String = ""

    private const val SLICE = 400

    private val picsPipeline = PublishSubject.create<XkcdPic>()

    val thumbUpList: Observable<List<XkcdPic>>
        get() = NetworkService.xkcdAPI
                .getTopXkcds(XKCD_TOP_SORT_BY_THUMB_UP)
                .subscribeOn(Schedulers.io())
                .map { BoxManager.updateAndSave(it) }

    val favXkcd: List<XkcdPic>
        get() = BoxManager.favXkcd

    val untouchedList: List<XkcdPic>
        get() = BoxManager.untouchedComicsList

    fun push(pic: XkcdPic) {
        picsPipeline.onNext(pic)
    }

    fun observe(): Observable<XkcdPic> = picsPipeline

    fun loadLatest(): Observable<XkcdPic> = NetworkService.xkcdAPI
            .latest
            .subscribeOn(Schedulers.io())
            .map { BoxManager.updateAndSave(it) }

    fun loadXkcd(index: Long): Observable<XkcdPic> = NetworkService.xkcdAPI
            .getXkcdList(XKCD_BROWSE_LIST, index.toInt(), 0, 1)
            .subscribeOn(Schedulers.io())
            .flatMap { xkcdPics ->
                if (xkcdPics.isEmpty()) {
                    NetworkService.xkcdAPI.getComics(index).subscribeOn(Schedulers.io())
                } else {
                    Observable.just<XkcdPic>(xkcdPics[0])
                }
            }.map { BoxManager.updateAndSave(it) }

    /**
     * fast loading all xkcd pics
     *
     * @param latestIndex
     * @return
     */
    fun fastLoad(latestIndex: Int): Single<Long> {

        val sectionCount = (latestIndex - 1) / SLICE + 1

        val lastSectionStartIndex = SLICE * (sectionCount - 1) + 1

        return Observable.range(0, (latestIndex - 1) / SLICE + 1)
                .subscribeOn(Schedulers.io())
                .map { i -> i * 400 + 1 }
                .flatMap { startIndex ->
                    Observable.just(BoxManager.getValidXkcdInRange(startIndex.toLong(), startIndex + SLICE - 1L))
                            .flatMap {
                                Timber.d("Start Index $startIndex, list size :${it.size}")
                                if (startIndex == 401 && it.size == 399 || it.size == SLICE) {
                                    Observable.just(true)
                                } else if (lastSectionStartIndex == startIndex && it.size == latestIndex - startIndex + 1) {
                                    Observable.just(true)
                                } else {
                                    loadRange(startIndex.toLong(), SLICE.toLong()).map { true }
                                }
                            }
                }
                .doOnNext { Timber.d("XkcdFastLoad") }
                .take(sectionCount.toLong())
                .count()
    }

    /**
     * @param start
     * @param range
     * @return last index.
     */
    fun loadRange(start: Long, range: Long, reversed: Int = 0): Observable<List<XkcdPic>> = NetworkService.xkcdAPI
            .getXkcdList(XKCD_BROWSE_LIST, start.toInt(), reversed, range.toInt())
            .subscribeOn(Schedulers.io())
            .map { BoxManager.updateAndSave(it) }

    /**
     * @param index
     * @return thumb up count
     */
    fun thumbsUp(index: Long): Observable<Long> = NetworkService.xkcdAPI
            .thumbsUp(index.toInt())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { BoxManager.likeXkcd(index) }
            .map { BoxManager.updateAndSave(it) }
            .map { it.thumbCount }

    fun validateXkcdList(xkcdList: List<XkcdPic>): Single<Boolean> = Observable.fromIterable(xkcdList)
            .subscribeOn(Schedulers.io())
            .filter { xkcdPic -> xkcdPic.width == 0 || xkcdPic.height == 0 }
            .toSortedList()
            .flatMap { xkcdPics ->
                if (xkcdPics.isEmpty()) {
                    Single.just(0)
                } else {
                    loadRange(xkcdPics[0].num, xkcdPics[xkcdPics.size - 1].num)
                            .singleOrError()
                }
            }.map { true }

    fun loadXkcdFromDB(index: Long) = BoxManager.getXkcd(index)

    fun loadXkcdFromDB(start: Long, end: Long) = BoxManager.getXkcdInRange(start, end)

    fun updateSize(index: Long, width: Int, height: Int) =
            BoxManager.getXkcd(index).also {
                if (it != null && width > 0 && height > 0) {
                    it.width = width
                    it.height = height
                    BoxManager.saveXkcd(it)
                }
            }

    fun fav(index: Long, isFav: Boolean): Observable<XkcdPic> {
        val xkcdPicInBox = BoxManager.favXkcd(index, isFav)
        return if (xkcdPicInBox == null || xkcdPicInBox.width == 0 || xkcdPicInBox.height == 0) {
            loadXkcd(index)
        } else {
            Observable.just(xkcdPicInBox)
        }
    }

    fun search(query: String): Observable<List<XkcdPic>> =
            NetworkService.xkcdAPI
                    .getXkcdsSearchResult(query)
                    .subscribeOn(Schedulers.io())
                    .map { BoxManager.updateAndSave(it) }

    fun loadExplain(index: Long, latestIndex: Long): Observable<String> {
        val url = XKCD_EXPLAIN_URL + index
        val observable = if (latestIndex - index in 0..9)
            NetworkService.xkcdAPI
                    .getExplainWithShortCache(url, 1800 * (latestIndex - index + 1))
        else
            NetworkService.xkcdAPI.getExplain(url)

        return observable.subscribeOn(Schedulers.io())
                .map { XkcdExplainUtil.getExplainFromHtml(it, url) }
    }

    fun loadLocalizedXkcd(index: Long): Maybe<XkcdPic> {
        return if (localizedUrl.isBlank()) {
            Maybe.error(Exception("Local xkcd not configured"))
        } else {
            NetworkService.xkcdAPI
                    .getLocalizedXkcd(localizedUrl.format(index))
                    .timeout(3000L, TimeUnit.MILLISECONDS)
                    .toMaybe()
        }
    }
}