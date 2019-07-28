package xyz.jienan.xkcd.whatif

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.Const.WHAT_IF_LATEST_INDEX
import xyz.jienan.xkcd.model.WhatIfModel
import java.util.concurrent.TimeUnit

class WhatIfFastLoadService : JobIntentService() {


    override fun onHandleWork(intent: Intent) {
        val latestId = intent.getLongExtra(WHAT_IF_LATEST_INDEX, 0)

        try {
            val e = Completable.timer(WHAT_IF_FASTLOAD_DELAY, TimeUnit.SECONDS, Schedulers.io())
                    .andThen(WhatIfModel.fastLoadWhatIfs(latestId))
                    .blockingGet(WHAT_IF_FASTLOAD_TIMEOUT, TimeUnit.SECONDS)
            if (e != null) {
                Timber.e(e, "Failed to fast load what ifs")
            } else {
                Timber.d("What ifs fast load finished")
            }
        } catch (e: Throwable) {
            Timber.e(e, "Failed to fast load what ifs")
        }
    }

    companion object {

        private const val WHAT_IF_FASTLOAD_DELAY = 3L

        private const val WHAT_IF_FASTLOAD_TIMEOUT = 23L

        private const val JOB_ID = 100

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, WhatIfFastLoadService::class.java, JOB_ID, intent)
        }
    }
}
