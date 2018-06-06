package xyz.jienan.xkcd.comics.fragment;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnPageChange;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.comics.ComicsPagerAdapter;
import xyz.jienan.xkcd.comics.contract.ComicsMainContract;
import xyz.jienan.xkcd.comics.presenter.ComicsMainPresenter;
import xyz.jienan.xkcd.comics.dialog.NumberPickerDialogFragment;
import xyz.jienan.xkcd.list.XkcdListActivity;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.ui.like.OnLikeListener;

import static android.content.Context.SENSOR_SERVICE;
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
import static xyz.jienan.xkcd.Const.FIRE_SHAKE;
import static xyz.jienan.xkcd.Const.FIRE_SPECIFIC_MENU;
import static xyz.jienan.xkcd.Const.FIRE_THUMB_UP;
import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NEW_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class ComicsMainFragment extends BaseFragment implements ComicsMainContract.View, ShakeDetector.Listener {

    private static final String LOADED_XKCD_ID = "xkcd_id";

    private static final String LATEST_XKCD_ID = "xkcd_latest_id";

    private static final int INVALID_ID = 0;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.btn_fav)
    LikeButton btnFav;

    @BindView(R.id.btn_thumb)
    LikeButton btnThumb;

    private ComicsPagerAdapter adapter;

    private ShakeDetector sd;

    private SharedPreferences sharedPreferences;

    // Use this field to record the latest xkcd pic id
    private int latestIndex = INVALID_ID;

    private int savedId = INVALID_ID;

    private ComicsMainContract.Presenter comicsMainPresenter;

    private boolean isFre = true;

    private boolean isPaused = true;

    private boolean isFabsShowing = false;

    private Toast toast;

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
                    comicsMainPresenter.comicFavorited(getCurrentIndex(), true);
                    logUXEvent(FIRE_FAVORITE_ON);
                    break;
                case R.id.btn_thumb:
                    comicsMainPresenter.comicLiked(getCurrentIndex());
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
                    comicsMainPresenter.comicFavorited(getCurrentIndex(), false);
                    logUXEvent(FIRE_FAVORITE_OFF);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_comic_main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container, savedInstanceState);
        setHasOptionsMenu(true);
        comicsMainPresenter = new ComicsMainPresenter(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        btnFav.setOnLikeListener(likeListener);
        btnThumb.setOnLikeListener(likeListener);
        adapter = new ComicsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        comicsMainPresenter.loadLatestXkcd();
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.menu_xkcd);
        }
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
                    (NumberPickerDialogFragment) getChildFragmentManager().findFragmentByTag("IdPickerDialogFragment");
            if (pickerDialog != null) {
                pickerDialog.setListener(pickerListener);
            }
            latestIndex = sharedPreferences.getInt(XKCD_LATEST_INDEX, INVALID_ID);

        } else {
            updateIndices(getActivity().getIntent());
        }
        isFre = latestIndex == INVALID_ID;
        if (latestIndex > INVALID_ID) {
            adapter.setSize(latestIndex);
            scrollViewPagerToItem(savedId > INVALID_ID ? savedId - 1 : latestIndex - 1, false);
        }
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.start(sensorManager);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0)
            outState.putInt(LOADED_XKCD_ID, viewPager.getCurrentItem() + 1);
        outState.putInt(LATEST_XKCD_ID, latestIndex);
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    public void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    public void onStop() {
        if (viewPager != null && latestIndex > INVALID_ID) {
            int lastViewed = viewPager.getCurrentItem() + 1;
            comicsMainPresenter.setLastViewed(lastViewed);
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        sd.stop();
        comicsMainPresenter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem itemRight = menu.findItem(R.id.action_right);
        ImageButton imageButtonRight = new ImageButton(getContext());
        imageButtonRight.setImageResource(R.drawable.ic_action_right);
        imageButtonRight.setBackground(null);

        itemRight.setActionView(imageButtonRight);
        imageButtonRight.setOnLongClickListener(v -> {
            scrollViewPagerToItem(latestIndex - 1, true);
            logUXEvent(FIRE_NEXT_BAR);
            return true;
        });
        imageButtonRight.setOnClickListener(v -> {
            String skipCount = getString(getResources().getIdentifier(sharedPreferences.getString(PREF_ARROW, "arrow_1"), "string", getActivity().getPackageName()));
            int skip = Integer.parseInt(skipCount);
            if (skip == 1) {
                scrollViewPagerToItem(viewPager.getCurrentItem() + skip, true);
            } else {
                scrollViewPagerToItem(viewPager.getCurrentItem() + skip, false);
            }
            logUXEvent(FIRE_NEXT_BAR);
        });

        MenuItem itemLeft = menu.findItem(R.id.action_left);
        ImageButton imageButtonLeft = new ImageButton(getContext());
        imageButtonLeft.setImageResource(R.drawable.ic_action_left);
        imageButtonLeft.setBackground(null);

        itemLeft.setActionView(imageButtonLeft);
        imageButtonLeft.setOnLongClickListener(v -> {
            scrollViewPagerToItem(0, true);
            logUXEvent(FIRE_PREVIOUS_BAR);
            return true;
        });
        imageButtonLeft.setOnClickListener(v -> {
            String skipCount = getString(getResources().getIdentifier(sharedPreferences.getString(PREF_ARROW, "arrow_1"), "string", getActivity().getPackageName()));
            int skip = Integer.parseInt(skipCount);
            if (skip == 1) {
                scrollViewPagerToItem(viewPager.getCurrentItem() - skip, true);
            } else {
                scrollViewPagerToItem(viewPager.getCurrentItem() - skip, false);
            }
            logUXEvent(FIRE_PREVIOUS_BAR);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                logUXEvent(FIRE_SEARCH);
                break;
            case R.id.action_xkcd_list:
                Intent intent = new Intent(getActivity(), XkcdListActivity.class);
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
                pickerDialogFragment.show(getChildFragmentManager(), "IdPickerDialogFragment");
                logUXEvent(FIRE_SPECIFIC_MENU);
                break;
            default:
                break;
        }
        return false;
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
        getActivity().getWindow().getDecorView().performHapticFeedback(CONTEXT_CLICK, FLAG_IGNORE_GLOBAL_SETTING);
        logUXEvent(FIRE_SHAKE);
    }

    @OnClick(R.id.fab)
    public void OnFABClicked() {
        toggleSubFabs(!isFabsShowing);
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SELECTED)
    public void OnPagerSelected(int position) {
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
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
            comicsMainPresenter.getInfoAndShowFab(getCurrentIndex());
        }
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
        comicsMainPresenter.fastLoad(latestIndex);
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
                comicsMainPresenter.setLatest(latestIndex);
                Map<String, String> params = new HashMap<>();
                params.put(FIRE_FROM_NOTIFICATION_INDEX, String.valueOf(notiIndex));
                logUXEvent(FIRE_FROM_NOTIFICATION, params);
            }

        } else {
            latestIndex = comicsMainPresenter.getLatest();
            savedId = comicsMainPresenter.getLastViewed(latestIndex);
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

    @SuppressLint("ObjectAnimatorBinding")
    private void fabAnimation(@ColorRes final int startColor, @ColorRes final int endColor, @DrawableRes final int icon) {
        final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint", getResources().getColor(startColor), getResources().getColor(endColor));
        animator.setDuration(1800L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            if (fab != null) {
                fab.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
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
                if (btnThumb != null && btnFav != null && showSubFabs) {
                    btnThumb.setVisibility(View.VISIBLE);
                    btnFav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (btnThumb != null && btnFav != null && !showSubFabs) {
                    btnThumb.setVisibility(View.GONE);
                    btnFav.setVisibility(View.GONE);
                }
            }
        });
        animSet.start();
    }


    @Override
    public void showThumbUpCount(Long thumbCount) {
        showToast(getContext(), String.valueOf(thumbCount));
    }

    private void scrollViewPagerToItem(int id, boolean smoothScroll) {
        viewPager.setCurrentItem(id, smoothScroll);
        fab.hide();
        toggleSubFabs(false);
        if (!smoothScroll) {
            comicsMainPresenter.getInfoAndShowFab(getCurrentIndex());
        }
    }
}
