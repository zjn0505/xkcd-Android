package xyz.jienan.xkcd.extra.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindString;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.extra.ExtraPagerAdapter;
import xyz.jienan.xkcd.extra.contract.ExtraMainContract;
import xyz.jienan.xkcd.extra.presenter.ExtraMainPresenter;
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment;
import xyz.jienan.xkcd.list.activity.XkcdListActivity;
import xyz.jienan.xkcd.model.ExtraComics;

import static xyz.jienan.xkcd.Const.FIRE_BROWSE_LIST_MENU;
import static xyz.jienan.xkcd.Const.LAST_VIEW_XKCD_ID;

public class ExtraMainFragment extends ContentMainBaseFragment implements ExtraMainContract.View {

    @BindString(R.string.search_hint_xkcd)
    String searchHint;

    @BindString(R.string.menu_xkcd)
    String titleText;

    private List<ExtraComics> searchSuggestions;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_comic_main;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        presenter = new ExtraMainPresenter(this);
        adapter = new ExtraPagerAdapter(getChildFragmentManager());
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null && viewPager.getCurrentItem() >= 0) {
            outState.putInt(LAST_VIEW_XKCD_ID, viewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_extra, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_go_xkcd).setVisible(false);
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
    public String getSearchHint() {
        return searchHint;
    }

    @Override
    public void renderXkcdSearch(List<ExtraComics> xkcdPics) {
        searchSuggestions = xkcdPics;
        String[] columns = {BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        };
        MatrixCursor cursor = new MatrixCursor(columns, xkcdPics.size());
//        for (int i = 0; i < searchSuggestions.size(); i++) {
//            XkcdPic xkcdPic = searchSuggestions.get(i);
//            String[] tmp = {Integer.toString(i), xkcdPic.getTargetImg(), xkcdPic.getTitle(), String.valueOf(xkcdPic.num)};
//            cursor.addRow(tmp);
//        }
        searchAdapter.swapCursor(cursor);
    }

    @Override
    public void showExtras(List<ExtraComics> extraComics) {
        adapter.setSize(extraComics.size());
    }

    @Override
    protected void suggestionClicked(int position) {
//        if (searchSuggestions != null && searchSuggestions.size() > position) {
//            XkcdPic xkcd = searchSuggestions.get(position);
//            scrollViewPagerToItem((int) (xkcd.num - 1), false);
//        }
    }
}
