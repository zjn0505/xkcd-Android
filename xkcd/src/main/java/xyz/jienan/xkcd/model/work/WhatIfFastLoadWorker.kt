package xyz.jienan.xkcd.model.work

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Single
import timber.log.Timber
import xyz.jienan.xkcd.model.WhatIfModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager

class WhatIfFastLoadWorker(appContext: Context, workerParams: WorkerParameters)
    : RxWorker(appContext, workerParams) {

    override fun createWork(): Single<Result> {
        return WhatIfModel.loadLatest()
                .doOnSubscribe { Timber.d("what if fast load start") }
                .map { it.num }
                .doOnSuccess { SharedPrefManager.latestWhatIf = it }
                .flatMapCompletable { WhatIfModel.fastLoadWhatIfs(it) }
                .toSingleDefault(Result.success())
                .doOnSuccess { Timber.d("what if fast load complete") }
                .onErrorReturnItem(Result.failure())
    }
}