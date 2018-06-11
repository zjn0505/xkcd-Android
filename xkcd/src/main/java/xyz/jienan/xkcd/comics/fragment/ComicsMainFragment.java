package xyz.jienan.xkcd.comics.fragment;

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
import android.database.MatrixCursor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnPageChange;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseFragment;
import xyz.jienan.xkcd.comics.ComicsPagerAdapter;
import xyz.jienan.xkcd.comics.SearchCursorAdapter;
import xyz.jienan.xkcd.comics.contract.ComicsMainContract;
import xyz.jienan.xkcd.comics.dialog.NumberPickerDialogFragment;
import xyz.jienan.xkcd.comics.presenter.ComicsMainPresenter;
import xyz.jienan.xkcd.list.XkcdListActivity;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.ui.like.LikeButton;
import xyz.jienan.xkcd.ui.like.OnLikeListener;

import static android.app.Activity.RESULT_OK;
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
import static xyz.jienan.xkcd.Const.INTENT_TARGET_XKCD_ID;
import static xyz.jienan.xkcd.Const.INVALID_ID;
import static xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID;
import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.XKCD_INDEX_ON_NOTI_INTENT;

public class ComicsMainFragment extends BaseFragment implements ComicsMainContract.View, ShakeDetector.Listener {

    private static final int REQ_LIST_ACTIVITY = 10;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.btn_fav)
    LikeButton btnFav;

    @BindView(R.id.btn_thumb)
    LikeButton btnThumb;

    @BindString(R.string.search_hint)
    String searchHint;

    private ComicsPagerAdapter adapter;

    private ShakeDetector sd;

    private SharedPreferences sharedPreferences;

    // Use this field to record the latest xkcd pic id
    private int latestIndex = INVALID_ID;

    private int lastViewdId = INVALID_ID;

    private ComicsMainContract.Presenter comicsMainPresenter;

    private boolean isFre = true;

    private boolean isPaused = true;

    private boolean isFabsShowing = false;

    private List<XkcdPic> searchSuggestions;

    private SearchCursorAdapter searchAdapter;

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
            if (actionBar != null) {
                actionBar.setSubtitle(String.valueOf(lastViewdId));
            }
            NumberPickerDialogFragment pickerDialog =
                    (NumberPickerDialogFragment) getChildFragmentManager().findFragmentByTag("IdPickerDialogFragment");
            if (pickerDialog != null) {
                pickerDialog.setListener(pickerListener);
            }
        } else {
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                int notiIndex = intent.getIntExtra(XKCD_INDEX_ON_NOTI_INTENT, INVALID_ID);

                if (notiIndex != INVALID_ID) {
                    lastViewdId = notiIndex;
                    latestIndex = lastViewdId;
                    comicsMainPresenter.setLatest(latestIndex);
                    Map<String, String> params = new HashMap<>();
                    params.put(FIRE_FROM_NOTIFICATION_INDEX, String.valueOf(notiIndex));
                    logUXEvent(FIRE_FROM_NOTIFICATION, params);
                }
                getActivity().setIntent(null);
            }
        }
        latestIndex = comicsMainPresenter.getLatest();
        lastViewdId = comicsMainPresenter.getLastViewed(latestIndex);
        isFre = latestIndex == INVALID_ID;
        if (latestIndex > INVALID_ID) {
            adapter.setSize(latestIndex);
            scrollViewPagerToItem(lastViewdId > INVALID_ID ? lastViewdId - 1 : latestIndex - 1, false);
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
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.getCurrentItem() + 1);
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
        inflater.inflate(R.menu.menu_xkcd, menu);

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

        setupComicsSearch(menu);
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
                startActivityForResult(intent, REQ_LIST_ACTIVITY);
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
        latestIndex = comicsMainPresenter.getLatest();
        if (latestIndex != INVALID_ID) {
            int randomId = new Random().nextInt(latestIndex + 1);
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
            scrollViewPagerToItem(latestIndex - 1, false);
        }
        comicsMainPresenter.setLatest(latestIndex);
        comicsMainPresenter.fastLoad(latestIndex);
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

    @Override
    public void renderXkcdSearch(List<XkcdPic> xkcdPics) {
        searchSuggestions = xkcdPics;
        String[] columns = {BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        };
        MatrixCursor cursor = new MatrixCursor(columns);
        for (int i = 0; i < searchSuggestions.size(); i++) {
            XkcdPic xkcdPic = searchSuggestions.get(i);
            String[] tmp = {Integer.toString(i), xkcdPic.getTargetImg(), xkcdPic.getTitle(), String.valueOf(xkcdPic.num)};
            cursor.addRow(tmp);
        }
        searchAdapter.swapCursor(cursor);
    }

    private void scrollViewPagerToItem(int id, boolean smoothScroll) {
        viewPager.setCurrentItem(id, smoothScroll);
        fab.hide();
        toggleSubFabs(false);
        if (!smoothScroll) {
            comicsMainPresenter.getInfoAndShowFab(getCurrentIndex());
        }
    }


    private void setupComicsSearch(Menu menu) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) {
            return;
        }
        searchView.setQueryHint(searchHint);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
                XkcdPic xkcd = searchSuggestions.get(position);
                searchView.clearFocus();
                searchItem.collapseActionView();
                scrollViewPagerToItem((int) (xkcd.num - 1), false);
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
                comicsMainPresenter.searchXkcd(newText);
                return true;
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

    private void setItemsVisibility(Menu menu, int[] hideItems, boolean visible) {
        for (int hideItem : hideItems) {
            menu.findItem(hideItem).setVisible(visible);
        }
    }
}
