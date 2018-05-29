package xyz.jienan.xkcd.home.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.base.BaseActivity;
import xyz.jienan.xkcd.home.ComicsPagerAdapter;
import xyz.jienan.xkcd.home.contract.MainActivityContract;
import xyz.jienan.xkcd.home.dialog.NumberPickerDialogFragment;
import xyz.jienan.xkcd.home.presenter.MainActivityPresenter;
import xyz.jienan.xkcd.list.XkcdListActivity;
import xyz.jienan.xkcd.settings.PreferenceActivity;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.ui.like.OnLikeListener;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.view.HapticFeedbackConstants.CONTEXT_CLICK;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static butterknife.OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED;
import static butterknife.OnPageChange.Callback.PAGE_SELECTED;
import static xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU;
import static xyz.jienan.xkcd.Const.FIRE_FAVORITE_OFF;
import static xyz.jienan.xkcd.Const.FIRE_FAVORITE_ON;
import static xyz.jienan.xkcd.Const.FIRE_FROM_NOTIFICATION;
import static xyz.jienan.xkcd.Const.FIRE_FROM_NOTIFICATION_INDEX;
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

public class MainActivity extends BaseActivity implements MainActivityContract.View, ShakeDetector.Listener {

    private final static int REQ_SETTINGS = 101;

    private static final String LOADED_XKCD_ID = "xkcd_id";

    private static final String LATEST_XKCD_ID = "xkcd_latest_id";

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

    private SharedPreferences sharedPreferences;

    private boolean isFre = true;

    private boolean isPaused = true;

    private boolean isFabsShowing = false;

    private Toast toast;

    private MainActivityContract.Presenter mainActivityPresenter;

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
                    mainActivityPresenter.comicFavorited(getCurrentIndex(), true);
                    logUXEvent(FIRE_FAVORITE_ON);
                    break;
                case R.id.btn_thumb:
                    mainActivityPresenter.comicLiked(getCurrentIndex());
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
                    mainActivityPresenter.comicFavorited(getCurrentIndex(), false);
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
        mainActivityPresenter = new MainActivityPresenter(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final ActionBar actionBar = getSupportActionBar();
        btnFav.setOnLikeListener(likeListener);
        btnThumb.setOnLikeListener(likeListener);
        adapter = new ComicsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        mainActivityPresenter.loadLatestXkcd();
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
            mainActivityPresenter.setLastViewed(lastViewed);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        sd.stop();
        mainActivityPresenter.onDestroy();
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

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SELECTED)
    public void OnPagerSelected(int position) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(String.valueOf(position + 1));
        }
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SCROLL_STATE_CHANGED)
    public void onPageScrollStateChanged(int state) {
        if (state == SCROLL_STATE_DRAGGING) {
            fab.hide();
            toggleSubFabs(false);
        } else if (state == SCROLL_STATE_IDLE) {
            mainActivityPresenter.getInfoAndShowFab(getCurrentIndex());
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

        MenuItem itemRight = menu.findItem(R.id.action_right);
        ImageButton imageButtonRight = new ImageButton(this);
        imageButtonRight.setImageResource(R.drawable.ic_action_right);
        imageButtonRight.setBackground(null);

        itemRight.setActionView(imageButtonRight);
        imageButtonRight.setOnLongClickListener(v -> {
            scrollViewPagerToItem(latestIndex - 1, true);
            logUXEvent(FIRE_NEXT_BAR);
            return true;
        });
        imageButtonRight.setOnClickListener(v -> {
            String skipCount = getString(getResources().getIdentifier(sharedPreferences.getString(PREF_ARROW, "arrow_1"), "string", getPackageName()));
            int skip = Integer.parseInt(skipCount);
            if (skip == 1) {
                scrollViewPagerToItem(viewPager.getCurrentItem() + skip, true);
            } else {
                scrollViewPagerToItem(viewPager.getCurrentItem() + skip, false);
            }
            logUXEvent(FIRE_NEXT_BAR);
        });

        MenuItem itemLeft = menu.findItem(R.id.action_left);
        ImageButton imageButtonLeft = new ImageButton(this);
        imageButtonLeft.setImageResource(R.drawable.ic_action_left);
        imageButtonLeft.setBackground(null);

        itemLeft.setActionView(imageButtonLeft);
        imageButtonLeft.setOnLongClickListener(v -> {
            scrollViewPagerToItem(0, true);
            logUXEvent(FIRE_PREVIOUS_BAR);
            return true;
        });
        imageButtonLeft.setOnClickListener(v -> {
            String skipCount = getString(getResources().getIdentifier(sharedPreferences.getString(PREF_ARROW, "arrow_1"), "string", getPackageName()));
            int skip = Integer.parseInt(skipCount);
            if (skip == 1) {
                scrollViewPagerToItem(viewPager.getCurrentItem() - skip, true);
            } else {
                scrollViewPagerToItem(viewPager.getCurrentItem() - skip, false);
            }
            logUXEvent(FIRE_PREVIOUS_BAR);
        });
        return true;
    }

    @Override
    public void latestXkcdLoaded(XkcdPic xkcdPic) {
        latestIndex = (int) xkcdPic.num;
        adapter.setSize(latestIndex);
        if (isFre) {
            if (savedId != INVALID_ID) {
                scrollViewPagerToItem(savedId - 1, false);
                savedId = INVALID_ID;
            } else {
                scrollViewPagerToItem(latestIndex - 1, false);
            }
        }
        mainActivityPresenter.fastLoad(latestIndex);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
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
                mainActivityPresenter.setLatest(latestIndex);
                Map<String, String> params = new HashMap<>();
                params.put(FIRE_FROM_NOTIFICATION_INDEX, String.valueOf(notiIndex));
                logUXEvent(FIRE_FROM_NOTIFICATION, params);
            }

        } else {
            latestIndex = mainActivityPresenter.getLatest();
            savedId = mainActivityPresenter.getLastViewed(latestIndex);
        }
    }

    @Override
    public void showFab(XkcdPic xkcdPic) {
        toggleFab(xkcdPic.isFavorite);
        btnFav.setLiked(xkcdPic.isFavorite);
        btnThumb.setLiked(xkcdPic.hasThumbed);
        fab.show();
    }

    @Override
    public void toggleFab(boolean isFavorite) {
        if (isFavorite) {
            fabAnimation(R.color.pink, R.color.white, R.drawable.ic_heart_on);
        } else {
            fabAnimation(R.color.white, R.color.pink, R.drawable.ic_heart_white);
        }
    }

    @Override
    public void showThumbUpCount(Long thumbCount) {
        showToast(MainActivity.this, String.valueOf(thumbCount));
    }

    private void scrollViewPagerToItem(int id, boolean smoothScroll) {
        viewPager.setCurrentItem(id, smoothScroll);
        fab.hide();
        toggleSubFabs(false);
        if (!smoothScroll) {
            mainActivityPresenter.getInfoAndShowFab(getCurrentIndex());
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private void fabAnimation(@ColorRes final int startColor, @ColorRes final int endColor, @DrawableRes final int icon) {
        final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint", getResources().getColor(startColor), getResources().getColor(endColor));
        animator.setDuration(1800L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
        });
        animator.start();
        fab.setImageResource(icon);
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

    private int getCurrentIndex() {
        return viewPager.getCurrentItem() + 1;
    }

    private void toggleSubFabs(final boolean showSubFabs) {
        btnThumb.setClickable(showSubFabs);
        btnFav.setClickable(showSubFabs);
        ObjectAnimator thumbMove, thumbAlpha, favMove, favAlpha;
        if (showSubFabs) {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, View.TRANSLATION_X, -215);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, View.ALPHA, 1);
            favMove = ObjectAnimator.ofFloat(btnFav, View.TRANSLATION_X, -150, -400);
            favAlpha = ObjectAnimator.ofFloat(btnFav, View.ALPHA, 1);
        } else {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, View.TRANSLATION_X, 0);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, View.ALPHA, 0);
            favMove = ObjectAnimator.ofFloat(btnFav, View.TRANSLATION_X, -150);
            favAlpha = ObjectAnimator.ofFloat(btnFav, View.ALPHA, 0);
        }

        isFabsShowing = showSubFabs;
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(thumbMove, thumbAlpha, favMove, favAlpha);
        animSet.setDuration(300);
        animSet.addListener(new AnimatorListenerAdapter() {
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
        });
        animSet.start();
    }
}
