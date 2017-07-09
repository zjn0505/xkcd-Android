package com.neuandroid.xkcd;

import android.content.Intent;
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
    private int currentIndex = 0;

    private String imgUrl = "";

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
        loadXkcdPic();
    }

    /**
     * Request current xkcd picture
     */
    private void loadXkcdPic() {
        try {
            URL url = new URL(NetworkUtils.XKCD_QUERY_BASE_URL);
            new XkcdQueryTask(listener).execute(url);
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
            new XkcdQueryTask(listener).execute(url);
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




    private IAsyncTaskListener listener = new IAsyncTaskListener() {
        @Override
        public void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Serializable result) {
            pbLoading.setVisibility(View.GONE);
            if (result instanceof XkcdPic) {
                if (0 == currentIndex) {
                    currentIndex = ((XkcdPic) result).num;
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
                int randomId = random.nextInt(currentIndex + 1);
                loadXkcdPicById(randomId);
                break;
        }
        return true;
    }
}
