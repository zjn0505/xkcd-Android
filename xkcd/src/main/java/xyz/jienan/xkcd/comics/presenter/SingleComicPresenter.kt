package xyz.jienan.xkcd.comics.presenter

import android.content.SharedPreferences
import android.graphics.Bitmap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import xyz.jienan.xkcd.Const.PREF_XKCD_SHOW_COMIC_ONLY
import xyz.jienan.xkcd.Const.PREF_XKCD_TRANSLATION
import xyz.jienan.xkcd.comics.contract.SingleComicContract
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.SharedPrefManager

class SingleComicPresenter(private val view: SingleComicContract.View, private val sharedPreferences: SharedPreferences) : SingleComicContract.Presenter, SharedPreferences.OnSharedPreferenceChangeListener {

    private val compositeDisposable = CompositeDisposable()

    private var loadPicDisposable = Disposables.disposed()

    private var loadLocalizedPicDisposable = Disposables.disposed()

    private var loadExplainDisposable = Disposables.disposed()

    override val showLocalXkcd: Boolean
        get() = sharedPreferences.getBoolean(PREF_XKCD_TRANSLATION, false)
                && XkcdModel.localizedUrl.isNotBlank()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private var index: Int? = null

    override fun getExplain(index: Long) {
        val latestIndex = SharedPrefManager.latestXkcd

        if (!loadExplainDisposable.isDisposed) {
            loadExplainDisposable.dispose()
        }

        XkcdModel.loadExplain(index, latestIndex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.explainLoaded(it) }, { e ->
                    view.explainFailed()
                    Timber.e(e, "Load explainUrl failed")
                })
                .also {
                    loadExplainDisposable = it
                    compositeDisposable.add(loadExplainDisposable)
                }
    }

    override fun loadXkcd(index: Int) {
        this.index = index
        val latestIndex = SharedPrefManager.latestXkcd
        val xkcdPicInDB = XkcdModel.loadXkcdFromDB(index.toLong())
        val shouldQueryNetwork = latestIndex - index < 10
        view.setLoading(true)

        if (!loadPicDisposable.isDisposed) {
            loadPicDisposable.dispose()
        }

        if (shouldQueryNetwork || xkcdPicInDB == null) {
            XkcdModel.loadXkcd(index.toLong())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { view.setLoading(false) }
                    .filter { xkcdPicInDB == null }
                    .subscribe({ this.renderComic(it) },
                            { e -> Timber.e(e, "load xkcd pic error") })
                    .also {
                        loadPicDisposable = it
                        compositeDisposable.add(it)
                    }
        }

        if (xkcdPicInDB != null) {
            renderComic(xkcdPicInDB)
        }
    }

    override fun updateXkcdSize(xkcdPic: XkcdPic?, resource: Bitmap?) {
        if (xkcdPic != null && (xkcdPic.width == 0 || xkcdPic.height == 0) && resource != null) {
            XkcdModel.updateSize(xkcdPic.num, resource.width, resource.height)
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences?, key: String?) {
        if (key == PREF_XKCD_TRANSLATION) {
            if (!showLocalXkcd) {
                view.translationMode = -1
            }
            if (index != null) {
                loadXkcd(index!!)
            }
        }
        if (key == PREF_XKCD_SHOW_COMIC_ONLY) {
            view.setAltTextVisibility(sharedPref!!.getBoolean(PREF_XKCD_SHOW_COMIC_ONLY, true))
        }
    }

    private fun renderComic(xkcdPic: XkcdPic) {
        XkcdModel.push(xkcdPic)
        if (showLocalXkcd) {
            loadLocalizedXkcd(xkcdPic)
        }
        if (view.translationMode != 1) {
            view.renderXkcdPic(xkcdPic)
        }
    }

    private fun loadLocalizedXkcd(xkcdPic: XkcdPic) {
        if (!loadLocalizedPicDisposable.isDisposed) {
            loadLocalizedPicDisposable.dispose()
        }
        XkcdModel.loadLocalizedXkcd(xkcdPic.num)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    XkcdPic(
                            year = xkcdPic.year,
                            month = xkcdPic.month,
                            day = xkcdPic.day,
                            width = xkcdPic.width,
                            height = xkcdPic.height,
                            isFavorite = xkcdPic.isFavorite,
                            hasThumbed = xkcdPic.hasThumbed,
                            num = xkcdPic.num,
                            _alt = it._alt,
                            _title = it._title,
                            img = it.img,
                            translated = true)
                }
                .subscribe({
                    if (view.translationMode == 1) {
                        view.renderXkcdPic(it)
                    } else {
                        view.translationMode = 0
                    }
                }, {
                    if (it is HttpException && it.code() == 400) {
                        Timber.i("Translation not available for ${xkcdPic.num}")
                    } else {
                        Timber.e(it)
                    }
                })
                .also {
                    loadLocalizedPicDisposable = it
                    compositeDisposable.add(it)
                }
    }
}
