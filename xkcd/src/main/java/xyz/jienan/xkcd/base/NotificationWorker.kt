package xyz.jienan.xkcd.base

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.Const
import xyz.jienan.xkcd.base.network.NetworkService
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.ui.NotificationUtils

class NotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : RxWorker(appContext, workerParams) {

    override fun createWork(): Single<Result> {
        Timber.d("Create work ${this.tags}")
        return NetworkService.xkcdAPI
                .latest
                .observeOn(Schedulers.io())
                .doOnNext { xkcdPic -> Timber.d("GetXkcdPic $xkcdPic") }
                .flatMapCompletable { checkLatestPicAndNotify(it) }
                .toSingleDefault(Result.success())
                .onErrorReturnItem(Result.failure())
    }

    private fun checkLatestPicAndNotify(xkcdPic: XkcdPic) : Completable {
        return Completable.fromAction {
            if (SharedPrefManager.latestXkcd < xkcdPic.num) {
                SharedPrefManager.latestXkcd = xkcdPic.num
                BoxManager.updateAndSave(xkcdPic)
                val allowNotification = PreferenceManager
                    .getDefaultSharedPreferences(applicationContext)
                    .getBoolean(Const.PREF_NOTIFICATION, true)
                        && NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
                if (allowNotification) {
                    NotificationUtils.showNotification(applicationContext, xkcdPic)
                }
            }
        }
    }
}