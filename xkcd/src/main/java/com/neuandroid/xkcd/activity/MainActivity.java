package com.neuandroid.xkcd.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.neuandroid.xkcd.NetworkUtils;
import com.neuandroid.xkcd.NumberPickerDialogFragment;
import com.neuandroid.xkcd.R;
import com.neuandroid.xkcd.SimpleInfoDialogFragment;
import com.neuandroid.xkcd.XkcdPic;
import com.neuandroid.xkcd.XkcdQueryTask;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivXkcdPic;
    private TextView tvCreateDate;
    private ProgressBar pbLoading;

    private final static String TAG = "MainActivity";

    // Use this field to record the latest xkcd pic id
    private int latestIndex = 0;

    private String imgUrl = "";
    private XkcdPic currentPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivXkcdPic = (ImageView) findViewById(R.id.iv_xkcd_pic);
        tvCreateDate = (TextView) findViewById(R.id.tv_create_date);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        ivXkcdPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDetailPageActivity();
            }
        });
        ivXkcdPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SimpleInfoDialogFragment dialogFragment = new SimpleInfoDialogFragment();
                dialogFragment.setContent(currentPic.alt);
                dialogFragment.setListener(dialogListener);
                dialogFragment.show(getSupportFragmentManager(), "AltInfoDialogFragment");
                return true;
            }
        });
        loadXkcdPic();
    }

    /**
     * Request current xkcd picture
     */
    private void loadXkcdPic() {
        try {
            URL url = new URL(NetworkUtils.XKCD_QUERY_BASE_URL);
            new XkcdQueryTask(queryListener).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request a specific xkcd picture
     * @param id the id of xkcd picture
     */
    private void loadXkcdPicById(int id) {
        String queryUrl = String.format(NetworkUtils.XKCD_QUERY_BY_ID_URL, id);

        try {
            URL url = new URL(queryUrl);
            new XkcdQueryTask(queryListener).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Render img, text on the view
     * @param xPic
     */
    private void renderXkcdPic(XkcdPic xPic) {
        tvTitle.setText(xPic.num + ". " + xPic.title);
        Glide.with(this).load(xPic.img).into(ivXkcdPic);
        imgUrl = xPic.img;
        currentPic = xPic;
        Log.d(TAG, "Pic to be loaded: " + imgUrl);
        tvCreateDate.setText("created on " + xPic.year + "." + xPic.month + "." + xPic.day);
    }


    /**
     * Launch a new Activity to show the pic in full screen mode
     */
    private void launchDetailPageActivity() {

        if (TextUtils.isEmpty(imgUrl)) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, ImageDetailPageActivity.class);
        intent.putExtra("URL", imgUrl);
        startActivity(intent);

    }


    private SimpleInfoDialogFragment.ISimpleInfoDialogListener dialogListener = new SimpleInfoDialogFragment.ISimpleInfoDialogListener() {
        @Override
        public void onPositiveClick() {
            // Do nothing
        }

        @Override
        public void onNegativeClick() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
            startActivity(browserIntent);
        }
    };


    private XkcdQueryTask.IAsyncTaskListener queryListener = new XkcdQueryTask.IAsyncTaskListener() {
        @Override
        public void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Serializable result) {
            pbLoading.setVisibility(View.GONE);
            if (result instanceof XkcdPic) {
                if (0 == latestIndex) {
                    latestIndex = ((XkcdPic) result).num;
                }
                renderXkcdPic((XkcdPic) result);
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_random:
                Random random = new Random();
                int randomId = random.nextInt(latestIndex + 1);
                loadXkcdPicById(randomId);
                break;
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Come and check this funny image I got from xkcd. \n " + currentPic.img);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));

                break;
            case R.id.action_specific:
                NumberPickerDialogFragment pickerDialogFragment = new NumberPickerDialogFragment();
                pickerDialogFragment.setTitle(getString(R.string.dialog_pick_title));
                pickerDialogFragment.setContent(getString(R.string.dialog_pick_content));
                pickerDialogFragment.setNumberRange(1, latestIndex);
                pickerDialogFragment.setListener(new NumberPickerDialogFragment.INumberPickerDialogListener() {
                    @Override
                    public void onPositiveClick(int number) {
                        loadXkcdPicById(number);
                    }

                    @Override
                    public void onNegativeClick() {
                        // Do nothing
                    }
                });
                pickerDialogFragment.show(getSupportFragmentManager(), "IdPickerDialogFragment");

                break;
            case R.id.action_go_xkcd: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xkcd.com/" + currentPic.num));
                startActivity(browserIntent);
                break;
            }
            case R.id.action_go_explain: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
                startActivity(browserIntent);
                break;
            }
        }
        return true;
    }
}
