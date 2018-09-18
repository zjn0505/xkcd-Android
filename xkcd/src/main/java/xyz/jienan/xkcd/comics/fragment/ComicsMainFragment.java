package xyz.jienan.xkcd.comics.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindString;
import butterknife.OnPageChange;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.comics.ComicsPagerAdapter;
import xyz.jienan.xkcd.comics.contract.ComicsMainContract;
import xyz.jienan.xkcd.comics.presenter.ComicsMainPresenter;
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment;
import xyz.jienan.xkcd.list.activity.XkcdListActivity;
import xyz.jienan.xkcd.model.XkcdPic;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static butterknife.OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED;
import static xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU;
import static xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID;

public class ComicsMainFragment extends ContentMainBaseFragment implements ComicsMainContract.View {

    @BindString(R.string.search_hint_xkcd)
    String searchHint;

    @BindString(R.string.menu_xkcd)
    String titleText;

    private List<XkcdPic> searchSuggestions;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_comic_main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        presenter = new ComicsMainPresenter(this);
        adapter = new ComicsPagerAdapter(getChildFragmentManager());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected String getTitleTextRes() {
        return titleText;
    }

    @Override
    protected int getPickerTitleTextRes() {
        return R.string.dialog_pick_content;
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
    public void latestXkcdLoaded(XkcdPic xkcdPic) {
        latestIndex = (int) xkcdPic.num;
        super.latestLoaded();
        ((ComicsMainPresenter) presenter).fastLoad(latestIndex);
    }

    @Override
    public void showFab(XkcdPic xkcdPic) {
        toggleFab(xkcdPic.isFavorite);
        btnFav.setLiked(xkcdPic.isFavorite);
        btnThumb.setLiked(xkcdPic.hasThumbed);
        fab.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0) {
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.getCurrentItem() + 1);
        }
    }

    @Override
    @OnPageChange(value = R.id.viewpager, callback = PAGE_SCROLL_STATE_CHANGED)
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (state == SCROLL_STATE_IDLE) {
            presenter.getInfoAndShowFab(getCurrentIndex());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_xkcd, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_xkcd_list:
                Intent intent = new Intent(getActivity(), XkcdListActivity.class);
                startActivityForResult(intent, REQ_LIST_ACTIVITY);
                logUXEvent(FIRE_BROWSE_LIST_MENU);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showThumbUpCount(Long thumbCount) {
        showToast(getContext(), String.valueOf(thumbCount));
    }

    @Override
    public String getSearchHint() {
        return searchHint;
    }

    @Override
    public void renderXkcdSearch(List<XkcdPic> xkcdPics) {
        searchSuggestions = xkcdPics;
        String[] columns = {BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        };
        MatrixCursor cursor = new MatrixCursor(columns, xkcdPics.size());
        for (int i = 0; i < searchSuggestions.size(); i++) {
            XkcdPic xkcdPic = searchSuggestions.get(i);
            String[] tmp = {Integer.toString(i), xkcdPic.getTargetImg(), xkcdPic.getTitle(), String.valueOf(xkcdPic.num)};
            cursor.addRow(tmp);
        }
        searchAdapter.swapCursor(cursor);
    }

    @Override
    protected void suggestionClicked(int position) {
        if (searchSuggestions != null && searchSuggestions.size() > position) {
            XkcdPic xkcd = searchSuggestions.get(position);
            scrollViewPagerToItem((int) (xkcd.num - 1), false);
        }
    }
}
