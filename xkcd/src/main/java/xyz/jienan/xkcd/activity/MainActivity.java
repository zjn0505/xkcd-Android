package xyz.jienan.xkcd.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Random;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.fragment.NumberPickerDialogFragment;
import xyz.jienan.xkcd.fragment.SingleComicFragment;
import xyz.jienan.xkcd.network.NetworkService;

import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.XKCD_LATEST_INDEX;

public class MainActivity extends BaseActivity {

    private ViewPager viewPager;
    private ComicsPagerAdapter adapter;

    private final static String TAG = "MainActivity";
    private final static int REQ_SETTINGS = 101;

    // Use this field to record the latest xkcd pic id
    private int latestIndex = 0;

    private static final String LOADED_XKCD_ID = "xkcd_id";
    private static final String LATEST_XKCD_ID = "xkcd_latest_id";
    private static final String LAST_VIEW_XKCD_ID = "xkcd_last_viewed_id";
    private int savedId = 0;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewpager);
        adapter = new ComicsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        final ActionBar actionBar = getSupportActionBar();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (actionBar != null) {
                    actionBar.setSubtitle(String.valueOf(position+1));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        loadXkcdPic();

        if (savedInstanceState != null) {
            savedId = savedInstanceState.getInt(LOADED_XKCD_ID);
            int i = savedInstanceState.getInt(LATEST_XKCD_ID);
            actionBar.setSubtitle(String.valueOf(savedId));
            if (i > 0) {
                latestIndex = i;
            }
        } else {
            savedId = sharedPreferences.getInt(LAST_VIEW_XKCD_ID, latestIndex);
        }
        if (savedInstanceState != null) {
            NumberPickerDialogFragment pickerDialog =
                    (NumberPickerDialogFragment) getSupportFragmentManager().findFragmentByTag("IdPickerDialogFragment");
            if (pickerDialog != null) {
                pickerDialog.setListener(pickerListener);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPager != null && latestIndex > 0) {
            int lastViewed = viewPager.getCurrentItem() + 1;
            if (editor == null) {
                editor = sharedPreferences.edit();
            }
            editor.putInt(LAST_VIEW_XKCD_ID, lastViewed).apply();
        }
    }

    private void loadXkcdPic() {
        NetworkService.getXkcdAPI().getLatest().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<XkcdPic>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(XkcdPic xkcdPic) {
                if (editor == null) {
                    editor = sharedPreferences.edit();
                }
                latestIndex = xkcdPic.num;
                editor.putInt(XKCD_LATEST_INDEX, latestIndex);
                editor.apply();
                adapter.setSize(latestIndex);
                if (savedId != 0) {
                    viewPager.setCurrentItem(savedId - 1, false);
                    savedId = 0;
                } else {
                    viewPager.setCurrentItem(latestIndex - 1, false);
                }

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_left:
                viewPager.setCurrentItem(viewPager.getCurrentItem() - Integer.valueOf(skipCount));
                break;
            case R.id.action_right:
                viewPager.setCurrentItem(viewPager.getCurrentItem() + Integer.valueOf(skipCount));
                break;
            case R.id.action_random:
                if (latestIndex == 0) {
                    loadXkcdPic();
                    break;
                }
                Random random = new Random();
                int randomId = random.nextInt(latestIndex + 1);
                viewPager.setCurrentItem(randomId - 1);
                break;
            case R.id.action_specific:
                if (latestIndex == 0) {
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

    private NumberPickerDialogFragment.INumberPickerDialogListener pickerListener =
            new NumberPickerDialogFragment.INumberPickerDialogListener() {
        @Override
        public void onPositiveClick(int number) {
            viewPager.setCurrentItem(number - 1, false);
        }

        @Override
        public void onNegativeClick() {
            // Do nothing
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SETTINGS) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }
    }
}
