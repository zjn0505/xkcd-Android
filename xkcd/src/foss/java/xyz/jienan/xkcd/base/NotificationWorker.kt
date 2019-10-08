package xyz.jienan.xkcd.base

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.base.network.NetworkService

class NotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : RxWorker(appContext, workerParams) {

    override fun createWork(): Single<Result> {
        Timber.d("Create work ${this.tags}")
        return NetworkService.xkcdAPI
                .latest
                .doOnNext { xkcdPic -> Timber.d("GetXkcdPic $xkcdPic") }
                .map { Result.success() }
                .singleOrError()
                .onErrorReturnItem(Result.failure())
                .observeOn(Schedulers.io())
    }
}