package xyz.jienan.xkcd.home.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import io.objectbox.Box;
import io.objectbox.query.Query;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.XkcdPic_;
import xyz.jienan.xkcd.base.BaseActivity;
import xyz.jienan.xkcd.home.ComicsPagerAdapter;
import xyz.jienan.xkcd.home.dialog.NumberPickerDialogFragment;
import xyz.jienan.xkcd.list.XkcdListActivity;
import xyz.jienan.xkcd.network.NetworkService;
import xyz.jienan.xkcd.settings.PreferenceActivity;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.ui.like.OnLikeListener;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.view.HapticFeedbackConstants.CONTEXT_CLICK;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU;
import static xyz.jienan.xkcd.Const.FIRE_FAVORITE_OFF;
import static xyz.jienan.xkcd.Const.FIRE_FAVORITE_ON;
import static xyz.jienan.xkcd.Const.FIRE_NEXT_BAR;
import static xyz.jienan.xkcd.Const.FIRE_PREVIOUS_BAR;
import static xyz.jienan.xkcd.Const.FIRE_SEARCH;
import static xyz.jienan.xkcd.Const.FIRE_SETTING_MENU;
import static xyz.jienan.xkcd.Const.FIRE_SHAKE;
import static xyz.jienan.xkcd.Const.FIRE_SPECIFIC_MENU;
import static xyz.jienan.xkcd.Const.FIRE_THUMB_UP;
import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;
import static xyz.jienan.xkcd.network.NetworkService.XKCD_BROWSE_LIST;

public class MainActivity extends BaseActivity implements ShakeDetector.Listener {

    private final static String TAG = "MainActivity";
    private final static int REQ_SETTINGS = 101;
    private static final String LOADED_XKCD_ID = "xkcd_id";
    private static final String LATEST_XKCD_ID = "xkcd_latest_id";
    private static final String LAST_VIEW_XKCD_ID = "xkcd_last_viewed_id";
    private static final int INVALID_ID = 0;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.btn_fav)
    LikeButton btnFav;
    @BindView(R.id.btn_thumb)
    LikeButton btnThumb;
    private ComicsPagerAdapter adapter;
    // Use this field to record the latest xkcd pic id
    private int latestIndex = INVALID_ID;
    private int savedId = INVALID_ID;
    private ShakeDetector sd;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean isFre = true;
    private boolean isPaused = true;
    private Disposable fabShowSubscription;
    private boolean isFabsShowing = false;
    private Box<XkcdPic> box;
    private Toast toast;
    private PicsPipeline pipeline = new PicsPipeline();
    private NumberPickerDialogFragment.INumberPickerDialogListener pickerListener =
            new NumberPickerDialogFragment.INumberPickerDialogListener() {
                @Override
                public void onPositiveClick(int number) {
                    scrollViewPagerToItem(number - 1, false);
                }

                @Override
                public void onNegativeClick() {
                    // Do nothing
                }
            };
    private OnLikeListener likeListener = new OnLikeListener() {
        @Override
        public void liked(LikeButton likeButton) {
            switch (likeButton.getId()) {
                case R.id.btn_fav:
                    comicFavorited(true);
                    logUXEvent(FIRE_FAVORITE_ON);
                    break;
                case R.id.btn_thumb:
                    comicLiked();
                    logUXEvent(FIRE_THUMB_UP);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            switch (likeButton.getId()) {
                case R.id.btn_fav:
                    comicFavorited(false);
                    logUXEvent(FIRE_FAVORITE_OFF);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        box = ((XkcdApplication) getApplication()).getBoxStore().boxFor(XkcdPic.class);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final ActionBar actionBar = getSupportActionBar();
        btnFav.setOnLikeListener(likeListener);
        btnThumb.setOnLikeListener(likeListener);
        adapter = new ComicsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        loadXkcdPic();
        if (savedInstanceState != null) {
            savedId = savedInstanceState.getInt(LOADED_XKCD_ID);
            int i = savedInstanceState.getInt(LATEST_XKCD_ID);
            if (actionBar != null) {
                actionBar.setSubtitle(String.valueOf(savedId));
            }
            if (i > INVALID_ID) {
                latestIndex = i;
            }
            NumberPickerDialogFragment pickerDialog =
                    (NumberPickerDialogFragment) getSupportFragmentManager().findFragmentByTag("IdPickerDialogFragment");
            if (pickerDialog != null) {
                pickerDialog.setListener(pickerListener);
            }
            latestIndex = sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);

        } else {
            updateIndices(getIntent());
        }
        isFre = latestIndex == INVALID_ID;
        if (latestIndex > INVALID_ID) {
            adapter.setSize(latestIndex);
            scrollViewPagerToItem(savedId > INVALID_ID ? savedId - 1 : latestIndex - 1, false);
        }
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.start(sensorManager);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateIndices(intent);
        isFre = latestIndex == INVALID_ID;
        if (latestIndex > INVALID_ID) {
            adapter.setSize(latestIndex);
            scrollViewPagerToItem(savedId > INVALID_ID ? savedId - 1 : latestIndex - 1, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (viewPager != null && latestIndex > INVALID_ID) {
            int lastViewed = viewPager.getCurrentItem() + 1;
            if (editor == null) {
                editor = sharedPreferences.edit();
            }
            editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        sd.stop();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0)
            outState.putInt(LOADED_XKCD_ID, viewPager.getCurrentItem() + 1);
        outState.putInt(LATEST_XKCD_ID, latestIndex);
    }

    @OnClick(R.id.fab)
    public void OnFABClicked() {
        toggleSubFabs(!isFabsShowing);
    }

    @OnPageChange(R.id.viewpager)
    public void OnPagerSelected(int position) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(String.valueOf(position + 1));
        }
    }

    @OnPageChange(R.id.viewpager)
    public void onPageScrollStateChanged(int state) {
        if (state == SCROLL_STATE_DRAGGING) {
            fab.hide();
            toggleSubFabs(false);
        } else if (state == SCROLL_STATE_IDLE) {
            getInfoAndShowFab();
        }
    }

    @Override
    public void hearShake() {
        if (isPaused) {
            return;
        }
        latestIndex = sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);
        if (latestIndex != INVALID_ID) {
            Random random = new Random();
            int randomId = random.nextInt(latestIndex + 1);
            scrollViewPagerToItem(randomId - 1, false);
        }
        getWindow().getDecorView().performHapticFeedback(CONTEXT_CLICK, FLAG_IGNORE_GLOBAL_SETTING);
        logUXEvent(FIRE_SHAKE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public int getMaxId() {
        return latestIndex;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String skipCount = getString(getResources().getIdentifier(sharedPreferences.getString(PREF_ARROW, "arrow_1"), "string", getPackageName()));
        int skip = Integer.valueOf(skipCount);
        int id = item.getItemId();
        switch (id) {
            case R.id.action_left:
                if (skip == 1) {
                    scrollViewPagerToItem(viewPager.getCurrentItem() - skip, true);
                } else {
                    scrollViewPagerToItem(viewPager.getCurrentItem() - skip, false);
                }
                logUXEvent(FIRE_PREVIOUS_BAR);
                break;
            case R.id.action_right:
                if (skip == 1) {
                    scrollViewPagerToItem(viewPager.getCurrentItem() + skip, true);
                } else {
                    scrollViewPagerToItem(viewPager.getCurrentItem() + skip, false);
                }
                logUXEvent(FIRE_NEXT_BAR);
                break;
            case R.id.action_search:
                logUXEvent(FIRE_SEARCH);
                break;
            case R.id.action_xkcd_list:
                Intent intent = new Intent(this, XkcdListActivity.class);
                startActivity(intent);
                logUXEvent(FIRE_BROWSE_LIST_MENU);
                break;
            case R.id.action_specific:
                if (latestIndex == INVALID_ID) {
                    break;
                }
                NumberPickerDialogFragment pickerDialogFragment = new NumberPickerDialogFragment();
                pickerDialogFragment.setNumberRange(1, latestIndex);
                pickerDialogFragment.setListener(pickerListener);
                pickerDialogFragment.show(getSupportFragmentManager(), "IdPickerDialogFragment");
                logUXEvent(FIRE_SPECIFIC_MENU);
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(settingsIntent, REQ_SETTINGS);
                logUXEvent(FIRE_SETTING_MENU);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SETTINGS) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }
    }

    private void loadXkcdPic() {
        Disposable d = NetworkService.getXkcdAPI()
                .getLatest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(xkcdPic -> {
                    if (editor == null) {
                        editor = sharedPreferences.edit();
                    }
                    latestIndex = (int) xkcdPic.num;
                    editor.putInt(XKCD_LATEST_INDEX, latestIndex);
                    editor.apply();
                    adapter.setSize(latestIndex);
                    if (isFre) {
                        if (savedId != INVALID_ID) {
                            scrollViewPagerToItem(savedId - 1, false);
                            savedId = INVALID_ID;
                        } else {
                            scrollViewPagerToItem(latestIndex - 1, false);
                        }
                    }
                    saveLatestXkcdDao(xkcdPic);
                }, e -> Timber.e(e, "load xkcd pic error"));
        compositeDisposable.add(d);
    }

    private void saveLatestXkcdDao(XkcdPic resXkcdPic) {
        XkcdPic xkcdPic = box.get(resXkcdPic.num);
        if (xkcdPic != null) {
            resXkcdPic.isFavorite = xkcdPic.isFavorite;
            resXkcdPic.hasThumbed = xkcdPic.hasThumbed;
        }
        box.put(resXkcdPic);
        for (int i = 0; i <= latestIndex / 400; i++) {
            loadList(i * 400 + 1);
        }
    }

    private void updateIndices(Intent intent) {
        if (intent != null && (intent.getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID) != INVALID_ID
                || intent.getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID) != INVALID_ID)) {
            int newIntentIndex = intent.getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID);
            int notiIndex = intent.getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID);
            if (newIntentIndex != INVALID_ID) {
                savedId = newIntentIndex;
            }
            if (notiIndex != INVALID_ID) {
                savedId = notiIndex;
                latestIndex = savedId;
                if (editor == null) {
                    editor = sharedPreferences.edit();
                }
                editor.putInt(XKCD_LATEST_INDEX, latestIndex);
                editor.apply();
            }

        } else {
            latestIndex = sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);
            savedId = sharedPreferences.getInt(LAST_VIEW_XKCD_ID, latestIndex);
        }
    }

    private int getCurrentIndex() {
        return viewPager.getCurrentItem() + 1;
    }

    private void comicLiked() {
        final XkcdPic xkcdPic = box.get(getCurrentIndex());
        xkcdPic.hasThumbed = true;
        box.put(xkcdPic);
        Disposable d = NetworkService.getXkcdAPI()
                .thumbsUp(NetworkService.XKCD_THUMBS_UP, getCurrentIndex())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(resXkcdPic -> {
                    resXkcdPic.isFavorite = xkcdPic.isFavorite;
                    resXkcdPic.hasThumbed = xkcdPic.hasThumbed;
                    box.put(resXkcdPic);
                    showToast(MainActivity.this, String.valueOf(resXkcdPic.thumbCount));
                }, e -> Timber.e(e, "Thumbs up failed"));
        compositeDisposable.add(d);
    }

    private void comicFavorited(boolean isFav) {
        final XkcdPic xkcdPic = box.get(getCurrentIndex());
        if (xkcdPic != null) {
            xkcdPic.isFavorite = isFav;
            box.put(xkcdPic);
            toggleFab(isFav);
            if (xkcdPic.width == 0 || xkcdPic.height == 0) {
                Disposable d = NetworkService.getXkcdAPI()
                        .getXkcdList(XKCD_BROWSE_LIST, (int) xkcdPic.num, 0, 1)
                        .subscribeOn(Schedulers.io())
                        .subscribe(xkcdPics -> {
                            if (xkcdPics != null && xkcdPics.size() == 1) {
                                XkcdPic xkcdPic1 = xkcdPics.get(0);
                                XkcdPic xkcdPicBox = box.get(xkcdPic1.num);
                                xkcdPicBox.width = xkcdPic1.width;
                                xkcdPicBox.height = xkcdPic1.height;
                                box.put(xkcdPicBox);
                            }
                        }, e -> Timber.e(e, "error on get one pic: %d", xkcdPic.num));
                compositeDisposable.add(d);
            }
        }
    }

    private void toggleSubFabs(final boolean showSubFabs) {
        btnThumb.setClickable(showSubFabs);
        btnFav.setClickable(showSubFabs);
        ObjectAnimator thumbMove, thumbAlpha, favMove, favAlpha;
        if (showSubFabs) {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, "translationX", -215);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, "alpha", 1);
            favMove = ObjectAnimator.ofFloat(btnFav, "translationX", -150, -400);
            favAlpha = ObjectAnimator.ofFloat(btnFav, "alpha", 1);
        } else {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, "translationX", 0);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, "alpha", 0);
            favMove = ObjectAnimator.ofFloat(btnFav, "translationX", -150);
            favAlpha = ObjectAnimator.ofFloat(btnFav, "alpha", 0);
        }

        isFabsShowing = showSubFabs;
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(thumbMove, thumbAlpha, favMove, favAlpha);
        animSet.setDuration(300);
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (showSubFabs) {
                    btnThumb.setVisibility(View.VISIBLE);
                    btnFav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!showSubFabs) {
                    btnThumb.setVisibility(View.GONE);
                    btnFav.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                // no op
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // no op
            }
        });
        animSet.start();
    }

    private void scrollViewPagerToItem(int id, boolean smoothScroll) {
        viewPager.setCurrentItem(id, smoothScroll);
        fab.hide();
        toggleSubFabs(false);
        if (!smoothScroll) {
            getInfoAndShowFab();
        }
    }

    private void getInfoAndShowFab() {
        if (fabShowSubscription != null && !fabShowSubscription.isDisposed()) {
            fabShowSubscription.dispose();
        }
        XkcdPic xkcdPic = box.get(getCurrentIndex());
        if (xkcdPic == null) {
            fabShowSubscription = getPipeline().toObservable()
                    .filter(xkcdPic1 -> xkcdPic1.num == getCurrentIndex())
                    .doOnNext(this::showFab)
                    .subscribe();
            compositeDisposable.add(fabShowSubscription);
        } else {
            showFab(xkcdPic);
        }
    }

    private void showFab(XkcdPic xkcdPic) {
        toggleFab(xkcdPic.isFavorite);
        btnFav.setLiked(xkcdPic.isFavorite);
        btnThumb.setLiked(xkcdPic.hasThumbed);
        fab.show();
    }

    @SuppressLint("ObjectAnimatorBinding")
    private void toggleFab(boolean isFavorite) {
        if (isFavorite) {
            final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint", getResources().getColor(R.color.pink), getResources().getColor(R.color.white));
            animator.setDuration(2000L);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setInterpolator(new DecelerateInterpolator(2));
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            });
            animator.start();
            fab.setImageResource(R.drawable.ic_heart_on);
        } else {
            final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint", getResources().getColor(R.color.white), getResources().getColor(R.color.pink));
            animator.setDuration(2000L);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setInterpolator(new DecelerateInterpolator(2));
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            });
            animator.start();
            fab.setImageResource(R.drawable.ic_heart_white);
        }
    }

    private void showToast(Context context, String text) {
        try {
            toast.getView().isShown();
            toast.setText(text);
        } catch (Exception e) {
            toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public PicsPipeline getPipeline() {
        return pipeline;
    }

    public static class PicsPipeline {
        private PublishSubject<XkcdPic> picsPipeline = PublishSubject.create();

        public void send(XkcdPic pic) {
            picsPipeline.onNext(pic);
        }

        public Observable<XkcdPic> toObservable() {
            return picsPipeline;
        }
    }

    @SuppressLint("CheckResult")
    private void loadList(final int start) {
        final Query<XkcdPic> query = box.query()
                .between(XkcdPic_.num, start, start + 399)
                .and().greater(XkcdPic_.width, 0).build();
        final List<XkcdPic> list = query.find();
        final HashMap<Long, XkcdPic> map = new HashMap<Long, XkcdPic>();
        for (XkcdPic xkcdPic : list) {
            map.put(xkcdPic.num, xkcdPic);
        }
        List<XkcdPic> data = query.find();

        int dataSize = data.size();
        Timber.d("Load xkcd list request, start from: %d, the response items: %d", start, dataSize);
        if ((start <= latestIndex - 399 && dataSize != 400 && start != 401) ||
                (start == 401 && dataSize != 399) ||
                (start > latestIndex - 399 && start + dataSize - 1 != latestIndex)) {
            Disposable d = NetworkService.getXkcdAPI()
                    .getXkcdList(XKCD_BROWSE_LIST, start, 0, 400)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .flatMap(Observable::fromIterable)
                    .filter(xkcdPic -> {
                        XkcdPic pic = map.get(xkcdPic.num);
                        return pic == null || pic.height == 0 || pic.width == 0;
                    }).toList()
                    .subscribe(xkcdPics -> {
                        for (XkcdPic pic : xkcdPics) {
                            XkcdPic picInBox = map.get(pic.num);
                            if (picInBox != null) {
                                pic.isFavorite = picInBox.isFavorite;
                                pic.hasThumbed = picInBox.hasThumbed;
                            }
                        }
                        box.put(xkcdPics);
                    }, e -> Timber.e(e, "load list error: start - %d", start));
            compositeDisposable.add(d);
        }
    }
}
