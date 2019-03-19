package xyz.jienan.xkcd.whatif;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.model.WhatIfModel;

import static xyz.jienan.xkcd.Const.WHAT_IF_LATEST_INDEX;

public class WhatIfFastLoadService extends IntentService {

    private static final int WHAT_IF_FASTLOAD_DELAY = 3;

    private final WhatIfModel whatIfModel;

    private Disposable disposable = Disposables.empty();

    public WhatIfFastLoadService() {
        super("WhatIfFastLoadService");
        whatIfModel = WhatIfModel.getInstance();
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            long latestId = intent.getLongExtra(WHAT_IF_LATEST_INDEX, 0);

            disposable = Observable.timer(WHAT_IF_FASTLOAD_DELAY, TimeUnit.SECONDS)
                    .flatMapCompletable(ignored -> whatIfModel.fastLoadWhatIfs(latestId))
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> Timber.d("what if fast load complete"),
                            e -> Timber.e(e, "what if fast load error"));
        }
    }

    @Override
    public void onDestroy() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}
