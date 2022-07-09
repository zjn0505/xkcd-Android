package xyz.jienan.xkcd.extra.presenter

import android.text.TextUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import timber.log.Timber
import xyz.jienan.xkcd.extra.contract.SingleExtraContract
import xyz.jienan.xkcd.model.ExtraModel

class SingleExtraPresenter(private val view: SingleExtraContract.View) : SingleExtraContract.Presenter {

    private var explainDisposable = Disposables.disposed()

    override fun getExplain(url: String, refresh: Boolean) {
        explainDisposable.dispose()
        ExtraModel.loadExplain(url, refresh)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { explainContent ->
                            view.explainLoaded(explainContent)
                            if (!TextUtils.isEmpty(explainContent)) {
                                ExtraModel.saveExtraWithExplain(url, explainContent)
                            }
                        },
                        { e ->
                            view.explainFailed()
                            Timber.e(e)
                        }
                ).also { explainDisposable = it }
    }

    override fun loadExtra(index: Int) {
        view.renderExtraPic(ExtraModel.getExtra(index))
    }

    override fun onDestroy() {
        explainDisposable.dispose()
    }
}
