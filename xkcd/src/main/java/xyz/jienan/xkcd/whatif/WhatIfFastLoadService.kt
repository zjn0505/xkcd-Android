package xyz.jienan.xkcd.whatif

import android.app.IntentService
import android.content.Intent
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.Const.WHAT_IF_LATEST_INDEX
import xyz.jienan.xkcd.model.WhatIfModel
import java.util.concurrent.TimeUnit

class WhatIfFastLoadService : IntentService("WhatIfFastLoadService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
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
            } catch (e: Exception) {
                Timber.e(e, "Failed to fast load what ifs")
            }
        }
    }

    companion object {

        private const val WHAT_IF_FASTLOAD_DELAY = 3L

        private const val WHAT_IF_FASTLOAD_TIMEOUT = 23L
    }
}
