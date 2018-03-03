package com.neuandroid.xkcd.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.neuandroid.xkcd.fragment.NumberPickerDialogFragment;
import com.neuandroid.xkcd.R;
import com.neuandroid.xkcd.fragment.SimpleInfoDialogFragment;
import com.neuandroid.xkcd.XkcdPic;
import com.neuandroid.xkcd.fragment.SingleComicFragment;
import com.neuandroid.xkcd.network.NetworkService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Callback;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ComicsPagerAdapter adapter;

    private final static String TAG = "MainActivity";

    // Use this field to record the latest xkcd pic id
    private int latestIndex = 0;

    private XkcdPic currentPic;

    private static final String LOADED_XKCD_ID = "xkcd_id";
    private static final String LATEST_XKCD_ID = "xkcd_latest_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        NetworkService.getXkcdAPI().getLatest().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<XkcdPic>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(XkcdPic xkcdPic) {
                latestIndex = xkcdPic.num;
                adapter.setSize(latestIndex);
                viewPager.setCurrentItem(latestIndex - 1);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        if (savedInstanceState != null) {
            int savedId = savedInstanceState.getInt(LOADED_XKCD_ID);
//            loadXkcdPicById(savedId);
            latestIndex = savedInstanceState.getInt(LATEST_XKCD_ID);
        } else {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPic != null)
            outState.putInt(LOADED_XKCD_ID, currentPic.num);
            outState.putInt(LATEST_XKCD_ID, latestIndex);
    }

    private class ComicsPagerAdapter extends FragmentStatePagerAdapter {

        private int length;

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

        @Override
        public Fragment getItem(int position) {
            SingleComicFragment fragment = SingleComicFragment.newInstance(position + 1);
            return fragment;
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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_left:
//                loadPreviousPic();
                break;
            case R.id.action_right:
//                loadNextPic();
                break;
            case R.id.action_random:
                if (latestIndex == 0) {
//                    loadXkcdPic();
                    break;
                }
                Random random = new Random();
                int randomId = random.nextInt(latestIndex + 1);
//                loadXkcdPicById(randomId);
                break;
            case R.id.action_share:
                if (currentPic == null) {
                    break;
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Come and check this funny image I got from xkcd. \n " + currentPic.getImg());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));

                break;
            case R.id.action_specific:
                if (currentPic == null) {
                    break;
                }
                NumberPickerDialogFragment pickerDialogFragment = new NumberPickerDialogFragment();
                pickerDialogFragment.setTitle(getString(R.string.dialog_pick_title));
                pickerDialogFragment.setContent(getString(R.string.dialog_pick_content));
                pickerDialogFragment.setNumberRange(1, latestIndex);
                pickerDialogFragment.setListener(new NumberPickerDialogFragment.INumberPickerDialogListener() {
                    @Override
                    public void onPositiveClick(int number) {
//                        loadXkcdPicById(number);
                    }

                    @Override
                    public void onNegativeClick() {
                        // Do nothing
                    }
                });
                pickerDialogFragment.show(getSupportFragmentManager(), "IdPickerDialogFragment");

                break;
            case R.id.action_go_xkcd: {
                if (currentPic == null) {
                    break;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xkcd.com/" + currentPic.num));
                startActivity(browserIntent);
                break;
            }
            case R.id.action_go_explain: {
                if (currentPic == null) {
                    break;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
                startActivity(browserIntent);
                break;
            }
        }
        return true;
    }
}
