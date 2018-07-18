package xyz.jienan.xkcd.whatif;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.model.WhatIfModel;

import static xyz.jienan.xkcd.Const.LAST_VIEW_WHAT_IF_ID;

public class WhatIfFastLoadService extends IntentService {

    private final WhatIfModel whatIfModel;

    public WhatIfFastLoadService() {
        super("WhatIfFastLoadService");
        whatIfModel = WhatIfModel.getInstance();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            long latestId = intent.getLongExtra(LAST_VIEW_WHAT_IF_ID, 0);

            Observable.timer(3, TimeUnit.SECONDS)
                    .flatMapCompletable(ignored -> whatIfModel.fastLoadWhatIfs(latestId))
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> Timber.d("complete"),
                            e -> Timber.e(e, "error"));
        }
    }
}
