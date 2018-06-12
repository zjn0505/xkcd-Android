package xyz.jienan.xkcd.comics.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindString;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.comics.ComicsPagerAdapter;
import xyz.jienan.xkcd.comics.contract.ComicsMainContract;
import xyz.jienan.xkcd.comics.presenter.ComicsMainPresenter;
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment;
import xyz.jienan.xkcd.model.XkcdPic;

import static android.app.Activity.RESULT_OK;
import static xyz.jienan.xkcd.Const.INTENT_TARGET_XKCD_ID;
import static xyz.jienan.xkcd.Const.INVALID_ID;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LIST_ACTIVITY && resultCode == RESULT_OK && data != null) {
            int targetId = data.getIntExtra(INTENT_TARGET_XKCD_ID, INVALID_ID);
            if (targetId != INVALID_ID) {
                scrollViewPagerToItem(targetId - 1, false);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0) {
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.getCurrentItem() + 1);
        }
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
        XkcdPic xkcd = searchSuggestions.get(position);
        scrollViewPagerToItem((int) (xkcd.num - 1), false);
    }
}
