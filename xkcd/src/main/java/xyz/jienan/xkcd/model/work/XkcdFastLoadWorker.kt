package xyz.jienan.xkcd.model.work

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Single
import timber.log.Timber
import xyz.jienan.xkcd.model.XkcdModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import java.util.concurrent.TimeUnit

class XkcdFastLoadWorker(appContext: Context, workerParams: WorkerParameters)
    : RxWorker(appContext, workerParams) {

    init {
        Timber.d("init")
    }

    override fun createWork(): Single<Result> {
        return XkcdModel.loadLatest()
                .doOnSubscribe { Timber.d("xkcd fast load start") }
                .map { it.num }
                .doOnNext { Timber.d("Latest xkcd $it") }
                .doOnNext { SharedPrefManager.latestXkcd = it }
                .doOnNext { Timber.d("Ready to fast load $it") }
                .flatMapSingle { XkcdModel.fastLoad(it.toInt()) }
                .doOnNext { Timber.d("Fast load xkcd complete $it") }
                .map { Result.success() }
                .singleOrError()
                .doOnSuccess { Timber.d("xkcd fast load complete") }
                .timeout(20L, TimeUnit.SECONDS)
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(Result.failure())
    }
}