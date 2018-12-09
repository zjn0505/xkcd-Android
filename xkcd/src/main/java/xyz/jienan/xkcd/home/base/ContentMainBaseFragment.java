package xyz.jienan.xkcd.home.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.SensorManager;
import android.os.Build;
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
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnPageChange;
import io.objectbox.Box;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.comics.SearchCursorAdapter;
import xyz.jienan.xkcd.comics.dialog.NumberPickerDialogFragment;
import xyz.jienan.xkcd.model.WhatIfArticle;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.persist.BoxManager;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.ui.NotificationUtils;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.ui.like.OnLikeListener;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.SENSOR_SERVICE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.view.HapticFeedbackConstants.CONTEXT_CLICK;
import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static butterknife.OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED;
import static butterknife.OnPageChange.Callback.PAGE_SELECTED;
import static xyz.jienan.xkcd.Const.FIRE_FAVORITE_OFF;
import static xyz.jienan.xkcd.Const.FIRE_FAVORITE_ON;
import static xyz.jienan.xkcd.Const.FIRE_FROM_NOTIFICATION;
import static xyz.jienan.xkcd.Const.FIRE_FROM_NOTIFICATION_INDEX;
import static xyz.jienan.xkcd.Const.FIRE_NEXT_BAR;
import static xyz.jienan.xkcd.Const.FIRE_NEXT_BAR_LONG;
import static xyz.jienan.xkcd.Const.FIRE_PREVIOUS_BAR;
import static xyz.jienan.xkcd.Const.FIRE_PREVIOUS_BAR_LONG;
import static xyz.jienan.xkcd.Const.FIRE_SEARCH;
import static xyz.jienan.xkcd.Const.FIRE_SHAKE;
import static xyz.jienan.xkcd.Const.FIRE_SPECIFIC_MENU;
import static xyz.jienan.xkcd.Const.FIRE_THUMB_UP;
import static xyz.jienan.xkcd.Const.FIRE_WHAT_IF_SUFFIX;
import static xyz.jienan.xkcd.Const.INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.INTENT_TARGET_XKCD_ID;
import static xyz.jienan.xkcd.Const.INVALID_ID;
import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.PREF_RANDOM;

public abstract class ContentMainBaseFragment extends BaseFragment implements ShakeDetector.Listener {

    protected static final int REQ_LIST_ACTIVITY = 10;

    @BindView(R.id.fab)
    public FloatingActionButton fab;

    @BindView(R.id.viewpager)
    protected ViewPager viewPager;

    @BindView(R.id.btn_fav)
    protected LikeButton btnFav;

    @BindView(R.id.btn_thumb)
    protected LikeButton btnThumb;

    protected BaseStatePagerAdapter adapter;

    protected ShakeDetector sd;

    protected int latestIndex = INVALID_ID;

    protected int lastViewedId = INVALID_ID;

    protected boolean isPaused = true;

    protected ContentMainBasePresenter presenter;

    protected boolean isFre = true;

    protected SearchCursorAdapter searchAdapter;

    private SharedPreferences sharedPreferences;

    private boolean isFabsShowing = false;

    private Toast toast;

    private BoxManager boxManager = BoxManager.getInstance();

    private AnimatorSet fabAnimSet;

    private OnLikeListener likeListener = new OnLikeListener() {
        @Override
        public void liked(LikeButton likeButton) {
            switch (likeButton.getId()) {
                case R.id.btn_fav:
                    presenter.favorited(getCurrentIndex(), true);
                    logSubUXEvent(FIRE_FAVORITE_ON);
                    break;
                case R.id.btn_thumb:
                    presenter.liked(getCurrentIndex());
                    logSubUXEvent(FIRE_THUMB_UP);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            switch (likeButton.getId()) {
                case R.id.btn_fav:
                    presenter.favorited(getCurrentIndex(), false);
                    logSubUXEvent(FIRE_FAVORITE_OFF);
                    break;
                default:
                    break;
            }
        }
    };

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

    protected abstract void suggestionClicked(int position);

    protected abstract String getTitleTextRes();

    protected abstract int getPickerTitleTextRes();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        sd = new ShakeDetector(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        btnFav.setOnLikeListener(likeListener);
        btnThumb.setOnLikeListener(likeListener);

        viewPager.setAdapter(adapter);
        presenter.loadLatest();
        latestIndex = presenter.getLatest();
        lastViewedId = presenter.getLastViewed(latestIndex);
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getTitleTextRes());
            actionBar.setSubtitle(null);
        }
        if (savedInstanceState != null) {
            if (actionBar != null) {
                actionBar.setSubtitle(String.valueOf(lastViewedId));
            }
            NumberPickerDialogFragment pickerDialog =
                    (NumberPickerDialogFragment) getChildFragmentManager().findFragmentByTag("IdPickerDialogFragment");
            if (pickerDialog != null) {
                pickerDialog.setListener(pickerListener);
            }
        } else if (getActivity().getIntent() != null) {
            int notiIndex = getActivity().getIntent().getIntExtra(INDEX_ON_NOTI_INTENT, INVALID_ID);

            if (notiIndex != INVALID_ID) {
                lastViewedId = notiIndex;
                latestIndex = lastViewedId;
                presenter.setLatest(latestIndex);
                Map<String, String> params = new HashMap<>();
                params.put(FIRE_FROM_NOTIFICATION_INDEX, String.valueOf(notiIndex));
                logSubUXEvent(FIRE_FROM_NOTIFICATION, params);
            }
            getActivity().setIntent(null);
        }
        isFre = latestIndex == INVALID_ID;
        if (latestIndex > INVALID_ID) {
            adapter.setSize(latestIndex);
            scrollViewPagerToItem(lastViewedId > INVALID_ID ? lastViewedId - 1 : latestIndex - 1, false);
        }
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

        sd.start(sensorManager);

        return view;
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
    public void onDestroyView() {
        sd.stop();
        presenter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        if (viewPager != null && latestIndex > INVALID_ID) {
            int lastViewed = viewPager.getCurrentItem() + 1;
            presenter.setLastViewed(lastViewed);
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LIST_ACTIVITY && resultCode == RESULT_OK && data != null) {
            int targetId = data.getIntExtra(INTENT_TARGET_XKCD_ID, INVALID_ID);
            if (targetId != INVALID_ID) {
                scrollViewPagerToItem(targetId - 1, false);
            }
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    protected void fabAnimation(@ColorRes final int startColor, @ColorRes final int endColor, @DrawableRes final int icon) {
        final ObjectAnimator animator = ObjectAnimator.ofInt(fab, "backgroundTint",
                getResources().getColor(startColor), getResources().getColor(endColor));
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem itemRight = menu.findItem(R.id.action_right);
        ImageButton imageButtonRight = new ImageButton(getContext());
        imageButtonRight.setImageResource(R.drawable.ic_action_right);
        imageButtonRight.setBackground(null);

        itemRight.setActionView(imageButtonRight);
        imageButtonRight.setOnLongClickListener(new MenuOnLongClickListener(this, false));
        imageButtonRight.setOnClickListener(new MenuClickListener(this, false));

        MenuItem itemLeft = menu.findItem(R.id.action_left);
        ImageButton imageButtonLeft = new ImageButton(getContext());
        imageButtonLeft.setImageResource(R.drawable.ic_action_left);
        imageButtonLeft.setBackground(null);

        itemLeft.setActionView(imageButtonLeft);
        imageButtonLeft.setOnLongClickListener(new MenuOnLongClickListener(this, true));
        imageButtonLeft.setOnClickListener(new MenuClickListener(this, true));
        setupSearch(menu);
    }

    private static class MenuClickListener implements View.OnClickListener {

        private WeakReference<ContentMainBaseFragment> weakReference;
        private boolean isPrevious;

        MenuClickListener(ContentMainBaseFragment fragment, boolean isPrevious) {
            weakReference = new WeakReference<>(fragment);
            this.isPrevious = isPrevious;
        }

        @Override
        public void onClick(View v) {
            if (weakReference.get() != null) {
                ContentMainBaseFragment fragment = weakReference.get();
                String skipCount = fragment.getString(fragment.getResources().
                        getIdentifier(fragment.sharedPreferences.getString(PREF_ARROW, "arrow_1"),
                                "string", fragment.getActivity().getPackageName()));
                int skip = Integer.parseInt(skipCount);
                if (skip == 1) {
                    skip = isPrevious ? 0 - skip : skip;
                    fragment.scrollViewPagerToItem(fragment.viewPager.getCurrentItem() + skip, true);
                } else {
                    fragment.scrollViewPagerToItem(fragment.viewPager.getCurrentItem() + skip, false);
                }
                fragment.logSubUXEvent(isPrevious ? FIRE_PREVIOUS_BAR : FIRE_NEXT_BAR);
            }
        }
    }

    private static class MenuOnLongClickListener implements View.OnLongClickListener {

        private WeakReference<ContentMainBaseFragment> weakReference;
        private boolean isPrevious;

        MenuOnLongClickListener(ContentMainBaseFragment fragment, boolean isPrevious) {
            weakReference = new WeakReference<>(fragment);
            this.isPrevious = isPrevious;
        }

        @Override
        public boolean onLongClick(View v) {
            if (weakReference.get() != null) {
                ContentMainBaseFragment fragment = weakReference.get();
                fragment.scrollViewPagerToItem(isPrevious ? 0 : fragment.latestIndex - 1, true);
                fragment.logSubUXEvent(isPrevious ? FIRE_PREVIOUS_BAR_LONG : FIRE_NEXT_BAR_LONG);
            }
            return true;
        }
    }

    @OnClick(R.id.fab)
    public void OnFABClicked() {
        toggleSubFabs(!isFabsShowing);
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SELECTED)
    public void OnPagerSelected(int position) {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(String.valueOf(position + 1));
        }
    }

    @OnPageChange(value = R.id.viewpager, callback = PAGE_SCROLL_STATE_CHANGED)
    public void onPageScrollStateChanged(int state) {
        if (state == SCROLL_STATE_DRAGGING) {
            fab.hide();
            toggleSubFabs(false);
        }
    }

    @Override
    public void hearShake() {
        if (isPaused) {
            return;
        }

        final String prefRandom = sharedPreferences.getString(PREF_RANDOM, "random_all");

        if ("random_disabled".equals(prefRandom)) {
            return;
        }

        latestIndex = presenter.getLatest();
        if (latestIndex != INVALID_ID) {
            int randomId = 0;

            if ("random_all".equals(prefRandom)) {
                randomId = new Random().nextInt(latestIndex + 1);
            } else {
                randomId = (int) presenter.getRandomUntouchedIndex();
            }
            scrollViewPagerToItem(randomId - 1, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity() != null) {
            getActivity().getWindow().getDecorView().performHapticFeedback(CONTEXT_CLICK, FLAG_IGNORE_GLOBAL_SETTING);
        }
        logSubUXEvent(FIRE_SHAKE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                logSubUXEvent(FIRE_SEARCH);
                break;
            case R.id.action_specific:
            case R.id.action_what_if_specific:
                if (latestIndex == INVALID_ID) {
                    break;
                }
                NumberPickerDialogFragment pickerDialogFragment =
                        (NumberPickerDialogFragment) getChildFragmentManager().findFragmentByTag("IdPickerDialogFragment");
                if (pickerDialogFragment == null) {
                    pickerDialogFragment = new NumberPickerDialogFragment();
                }
                pickerDialogFragment.setNumberRange(1, latestIndex);
                pickerDialogFragment.setListener(pickerListener);
                pickerDialogFragment.setTitle(getPickerTitleTextRes());
                pickerDialogFragment.show(getChildFragmentManager(), "IdPickerDialogFragment");
                logSubUXEvent(FIRE_SPECIFIC_MENU);
                break;
            case R.id.test_noti:
                if (getTitleTextRes().equals("xkcd")) {
                    XkcdPic xkcd = boxManager.getXkcd(latestIndex);
                    Box xkcdBox = XkcdApplication.getInstance().getBoxStore().boxFor(XkcdPic.class);
                    xkcdBox.remove(latestIndex);
                    latestIndex = latestIndex - 1;
                    presenter.setLatest(latestIndex);
                    presenter.setLastViewed(10);
                    expand(latestIndex);
                    xkcdNoti(xkcd);
                } else if (getTitleTextRes().equals("what if")) {
                    WhatIfArticle whatIfArticle = boxManager.getWhatIf(latestIndex);
                    Box whatIfBox = XkcdApplication.getInstance().getBoxStore().boxFor(WhatIfArticle.class);
                    whatIfBox.remove(latestIndex);
                    latestIndex = latestIndex - 1;
                    presenter.setLatest(latestIndex);
                    presenter.setLastViewed(10);
                    expand(latestIndex);
                    whatIfNoti(whatIfArticle);
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void xkcdNoti(XkcdPic xkcdPic) {
        SharedPrefManager sharedPrefManager = new SharedPrefManager();
        if (latestIndex >= xkcdPic.num) {
            return; // User already read the latest comic
        } else {
            sharedPrefManager.setLatestXkcd(latestIndex);
            boxManager.updateAndSave(xkcdPic);
        }
        if (getActivity() != null) {
            NotificationUtils.showNotification(getActivity(), xkcdPic);
        }
    }

    private void whatIfNoti(WhatIfArticle whatIfArticle) {
        SharedPrefManager sharedPrefManager = new SharedPrefManager();
        if (latestIndex >= whatIfArticle.num) {
            return; // User already read the what if
        } else {
            sharedPrefManager.setLatestWhatIf(latestIndex);
            boxManager.updateAndSaveWhatIf(Collections.singletonList(whatIfArticle));
        }
        if (getActivity() != null) {
            NotificationUtils.showNotification(getActivity(), whatIfArticle);
        }
    }

    public void scrollViewPagerToItem(int id, boolean smoothScroll) {
        viewPager.setCurrentItem(id, smoothScroll);
        fab.hide();
        toggleSubFabs(false);
        if (!smoothScroll) {
            presenter.getInfoAndShowFab(getCurrentIndex());
        }
    }

    public void expand(int size) {
        adapter.setSize(size);
    }

    protected void toggleSubFabs(final boolean showSubFabs) {
        btnThumb.setClickable(showSubFabs);
        btnFav.setClickable(showSubFabs);
        ObjectAnimator thumbMove, thumbAlpha, favMove, favAlpha;
        int distance = fab.getWidth();
        if (showSubFabs) {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, View.TRANSLATION_X, -distance);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, View.ALPHA, 0, 1);
            favMove = ObjectAnimator.ofFloat(btnFav, View.TRANSLATION_X, -distance, -distance * 2);
            favAlpha = ObjectAnimator.ofFloat(btnFav, View.ALPHA, 0, 1);
        } else {
            thumbMove = ObjectAnimator.ofFloat(btnThumb, View.TRANSLATION_X, 0);
            thumbAlpha = ObjectAnimator.ofFloat(btnThumb, View.ALPHA, 0);
            favMove = ObjectAnimator.ofFloat(btnFav, View.TRANSLATION_X, -distance);
            favAlpha = ObjectAnimator.ofFloat(btnFav, View.ALPHA, 0);
        }

        isFabsShowing = showSubFabs;

        if (fabAnimSet != null && fabAnimSet.isRunning()) {
            fabAnimSet.cancel();
        }

        fabAnimSet = new AnimatorSet();
        fabAnimSet.playTogether(thumbMove, thumbAlpha, favMove, favAlpha);
        fabAnimSet.setDuration(300).addListener(new AnimatorListenerAdapter() {
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
        fabAnimSet.start();
    }

    protected void showToast(Context context, String text) {
        try {
            toast.getView().isShown();
            toast.setText(text);
        } catch (Exception e) {
            toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        toast.show();
    }


    protected int getCurrentIndex() {
        return viewPager.getCurrentItem() + 1;
    }

    protected void latestLoaded() {
        if (adapter != null) {
            adapter.setSize(latestIndex);
        }
        if (isFre) {
            scrollViewPagerToItem(latestIndex - 1, false);
        }
        presenter.setLatest(latestIndex);
    }

    @SuppressLint("RestrictedApi")
    private void setupSearch(Menu menu) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) {
            return;
        }
        searchView.setQueryHint(getSearchHint());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        SearchView.SearchAutoComplete searchSrcTextView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        if (searchSrcTextView != null) {
            searchSrcTextView.setThreshold(1);
        }
        if (searchAdapter == null) {
            searchAdapter = new SearchCursorAdapter(getActivity(), null, 0);
        }
        searchView.setSuggestionsAdapter(searchAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                suggestionClicked(position);
                searchView.clearFocus();
                searchItem.collapseActionView();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    return true;
                }
                presenter.searchContent(newText);
                return false;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                setItemsVisibility(menu, new int[]{R.id.action_left, R.id.action_right, R.id.action_share}, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                setItemsVisibility(menu, new int[]{R.id.action_left, R.id.action_right, R.id.action_share}, true);
                return true;
            }
        });
    }

    protected abstract CharSequence getSearchHint();

    private void setItemsVisibility(Menu menu, int[] hideItems, boolean visible) {
        for (int hideItem : hideItems) {
            menu.findItem(hideItem).setVisible(visible);
        }
    }

    private void logSubUXEvent(String event) {
        logSubUXEvent(event, null);
    }

    private void logSubUXEvent(String event, Map<String, String> params) {
        String suffix = getTitleTextRes().equals("xkcd") ? "" : FIRE_WHAT_IF_SUFFIX;
        logUXEvent(event + suffix, params);
    }
}
