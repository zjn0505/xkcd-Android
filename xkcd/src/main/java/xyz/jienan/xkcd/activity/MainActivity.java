package xyz.jienan.xkcd.activity;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.fragment.NumberPickerDialogFragment;
import xyz.jienan.xkcd.fragment.SingleComicFragment;
import xyz.jienan.xkcd.network.NetworkService;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.ui.like.OnLikeListener;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.view.HapticFeedbackConstants.CONTEXT_CLICK;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class MainActivity extends BaseActivity implements ShakeDetector.Listener {

    private final static String TAG = "MainActivity";
    private final static int REQ_SETTINGS = 101;
    private static final String LOADED_XKCD_ID = "xkcd_id";
    private static final String LATEST_XKCD_ID = "xkcd_latest_id";
    private static final String LAST_VIEW_XKCD_ID = "xkcd_last_viewed_id";
    private static final int INVALID_ID = 0;
    private ViewPager viewPager;
    private FloatingActionButton fab;
    private LikeButton btnFav;
    private LikeButton btnThumb;
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
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        box = ((XkcdApplication) getApplication()).getBoxStore().boxFor(XkcdPic.class);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewpager);
        btnFav = findViewById(R.id.btn_fav);
        btnThumb = findViewById(R.id.btn_thumb);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSubFabs(!isFabsShowing);
            }
        });
        btnFav.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                comicFavorited(true);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                comicFavorited(false);
            }
        });
        btnThumb.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                comicLiked();
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                // no ops
            }
        });
        adapter = new ComicsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        final ActionBar actionBar = getSupportActionBar();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // no op
            }

            @Override
            public void onPageSelected(int position) {
                if (actionBar != null) {
                    actionBar.setSubtitle(String.valueOf(position + 1));
                }
                Timber.d("selected");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == SCROLL_STATE_DRAGGING) {
                    fab.hide();
                    toggleSubFabs(false);
                } else if (state == SCROLL_STATE_IDLE) {
                    getInfoAndShowFab();
                }

                Timber.d("state " + state);
            }
        });
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
            if (getIntent() != null && (getIntent().getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID) != INVALID_ID
                    || getIntent().getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID) != INVALID_ID)) {
                int newIntentIndex = getIntent().getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID);
                int notiIndex = getIntent().getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID);
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
        isFre = latestIndex == INVALID_ID;
        if (latestIndex > INVALID_ID) {
            adapter.setSize(latestIndex);
            scrollViewPagerToItem(savedId > INVALID_ID ? savedId - 1 : latestIndex - 1, false);
        }
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.start(sensorManager);
    }

    private int getCurrentIndex() {
        return viewPager.getCurrentItem() + 1;
    }

    private void comicLiked() {
        final XkcdPic xkcdPic = box.get(getCurrentIndex());
        xkcdPic.hasThumbed = true;
        box.put(xkcdPic);
        Observable<XkcdPic> thumbsUpObservable = NetworkService.getXkcdAPI()
                .thumbsUp(NetworkService.XKCD_THUMBS_UP, getCurrentIndex())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        thumbsUpObservable.subscribe(new Observer<XkcdPic>() {
            @Override
            public void onSubscribe(Disposable d) {
                // no ops
            }

            @Override
            public void onNext(XkcdPic resXkcdPic) {
                resXkcdPic.isFavorite = xkcdPic.isFavorite;
                resXkcdPic.hasThumbed = xkcdPic.hasThumbed;
                box.put(resXkcdPic);
                showToast(MainActivity.this, String.valueOf(resXkcdPic.thumbCount));
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Thumbs up failed", e);
            }

            @Override
            public void onComplete() {
                // no ops
            }
        });
    }

    private void comicFavorited(boolean isFav) {
        XkcdPic xkcdPic = box.get(getCurrentIndex());
        xkcdPic.isFavorite = isFav;
        box.put(xkcdPic);
        toggleFab(isFav);
    }

    private void toggleSubFabs(boolean showSubFabs) {
        ArrayList<ObjectAnimator> objectAnimatorsArray = new ArrayList<ObjectAnimator>();
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
        objectAnimatorsArray.add(thumbMove);
        objectAnimatorsArray.add(thumbAlpha);
        objectAnimatorsArray.add(favMove);
        objectAnimatorsArray.add(favAlpha);
        isFabsShowing = showSubFabs;
        ObjectAnimator[] objectAnimators = objectAnimatorsArray.toArray(new ObjectAnimator[objectAnimatorsArray.size()]);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(objectAnimators);
        animSet.setDuration(300);
        animSet.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID) != INVALID_ID
                || intent.getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID) != INVALID_ID) {
            int newIntentIndex = intent.getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID);
            int notiIntentIndex = intent.getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID);
            if (newIntentIndex != INVALID_ID) {
                savedId = newIntentIndex;
                latestIndex = sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);
            }
            if (notiIntentIndex != INVALID_ID) {
                savedId = intent.getIntExtra(XKCD_INDEX_ON_NEW_INTENT, INVALID_ID);
                latestIndex = savedId;
                if (editor == null) {
                    editor = sharedPreferences.edit();
                }
                editor.putInt(XKCD_LATEST_INDEX, latestIndex);
                editor.apply();
            }
        }
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
    protected void onDestroy() {
        compositeDisposable.dispose();
        if (viewPager != null && latestIndex > INVALID_ID) {
            int lastViewed = viewPager.getCurrentItem() + 1;
            if (editor == null) {
                editor = sharedPreferences.edit();
            }
            editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply();
        }
        sd.stop();
        super.onDestroy();
    }

    private void loadXkcdPic() {
        NetworkService.getXkcdAPI().getLatest().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<XkcdPic>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(XkcdPic xkcdPic) {
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
            }

            private void saveLatestXkcdDao(XkcdPic resXkcdPic) {
                XkcdPic xkcdPic = box.get(resXkcdPic.num);
                if (xkcdPic != null) {
                    resXkcdPic.isFavorite = xkcdPic.isFavorite;
                    resXkcdPic.hasThumbed = xkcdPic.hasThumbed;
                }
                box.put(resXkcdPic);
            }

            @Override
            public void onError(Throwable e) {
                // no op
            }

            @Override
            public void onComplete() {
                // no op
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0)
            outState.putInt(LOADED_XKCD_ID, viewPager.getCurrentItem() + 1);
        outState.putInt(LATEST_XKCD_ID, latestIndex);
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
                break;
            case R.id.action_right:
                if (skip == 1) {
                    scrollViewPagerToItem(viewPager.getCurrentItem() + skip, true);
                } else {
                    scrollViewPagerToItem(viewPager.getCurrentItem() + skip, false);
                }
                break;
            case R.id.action_search:
                break;
            case R.id.action_xkcd_list:
                Intent intent = new Intent(this, XkcdListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_specific:
                if (latestIndex == INVALID_ID) {
                    break;
                }
                NumberPickerDialogFragment pickerDialogFragment = new NumberPickerDialogFragment();
                pickerDialogFragment.setNumberRange(1, latestIndex);
                pickerDialogFragment.setListener(pickerListener);
                pickerDialogFragment.show(getSupportFragmentManager(), "IdPickerDialogFragment");
                break;
            case R.id.action_settings: {
                Intent settingsIntent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(settingsIntent, REQ_SETTINGS);
                break;
            }
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

    private void scrollViewPagerToItem(int id, boolean smoothScroll) {
        viewPager.setCurrentItem(id, smoothScroll);
        fab.hide();
        toggleSubFabs(false);
        if (!smoothScroll) {
            if (fabShowSubscription != null && !fabShowSubscription.isDisposed()) {
                fabShowSubscription.dispose();
            }
            fabShowSubscription = Observable.timer(400, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long ignored) throws Exception {
                    getInfoAndShowFab();
                }
            });
        }
    }

    private void getInfoAndShowFab() {
        XkcdPic xkcdPic = box.get(getCurrentIndex());
        toggleFab(xkcdPic.isFavorite);
        btnFav.setLiked(xkcdPic.isFavorite);
        btnThumb.setLiked(xkcdPic.hasThumbed);
        fab.show();
    }

    private void toggleFab(boolean isFavorite) {
        if (isFavorite) {
            final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint", getResources().getColor(R.color.pink), getResources().getColor(R.color.white));
            animator.setDuration(2000L);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setInterpolator(new DecelerateInterpolator(2));
            animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                }
            });
            animator.start();
            fab.setImageResource(R.drawable.ic_heart_on);
        } else {
            final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint", getResources().getColor(R.color.white), getResources().getColor(R.color.pink));
            animator.setDuration(2000L);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setInterpolator(new DecelerateInterpolator(2));
            animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                }
            });
            animator.start();
            fab.setImageResource(R.drawable.ic_heart_white);
        }
    }

    public final void showToast(Context context, String text) {
        try {
            toast.getView().isShown();
            toast.setText(text);
        } catch (Exception e) {
            toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    private class ComicsPagerAdapter extends FragmentStatePagerAdapter {

        private int length;
        private HashMap<Integer, SingleComicFragment> fragmentsMap = new HashMap<>();

        public ComicsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public ComicsPagerAdapter(FragmentManager fm, int size) {
            super(fm);
            length = size;
        }

        public void setSize(int size) {
            length = size;
            notifyDataSetChanged();
        }

        public SingleComicFragment getFragment(int id) {
            return fragmentsMap.get(id);
        }

        @Override
        public Fragment getItem(int position) {
            SingleComicFragment fragment = SingleComicFragment.newInstance(position + 1);
            fragmentsMap.put(position + 1, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragmentsMap.remove(position + 1);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return length;
        }
    }
}
