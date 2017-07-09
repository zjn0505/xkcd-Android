package com.neuandroid.xkcd;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivXkcdPic;
    private TextView tvCreateDate;
    private ProgressBar pbLoading;

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivXkcdPic = (ImageView) findViewById(R.id.iv_xkcd_pic);
        tvCreateDate = (TextView) findViewById(R.id.tv_create_date);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);

        loadXkcdPic();
    }

    /**
     * Request current xkcd picture
     */
    private void loadXkcdPic() {
        try {
            URL url = new URL(NetworkUtils.XKCD_QUERY_BASE_URL);
            new XkcdQueryTask().execute(url);
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
            new XkcdQueryTask().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    /**
     * This class extends AsyncTask to execute the query out of the main thread.
     * We should always not run a time consuming task on main thread.
     */
    private class XkcdQueryTask extends AsyncTask<URL, Object, String> {

        @Override
        protected void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            String result = null;

            try {
                URL url = params[0];
                result = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            pbLoading.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(result)) {

                try {
                    XkcdPic xkcdResult = extractXkcdPicFromJson(result);
                    renderXkcdPic(xkcdResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Extract a XkcdPic object from the json
         * This method looks chunky for now, we will improve it later.
         * @param json json string from query
         * @return Xkcd object
         */
        private XkcdPic extractXkcdPicFromJson(String json) throws JSONException {
            XkcdPic xPic = new XkcdPic();

            JSONObject jsonObject = new JSONObject(json);
            String year = jsonObject.optString("year");
            String month = jsonObject.optString("month");
            String day = jsonObject.optString("day");
            int num = jsonObject.optInt("num");
            String title = jsonObject.optString("title");
            String img = jsonObject.optString("img");
            String alt = jsonObject.optString("alt");

            xPic.year = year;
            xPic.month = month;
            xPic.day = day;
            xPic.num = num;
            xPic.title = title;
            xPic.img = img;
            xPic.alt = alt;

            return xPic;
        }
    }

    private class XkcdPic {
        public String year;
        public String month;
        public String day;
        public int num;
        public String title;
        public String img;
        public String alt;
    }

    /**
     * Render img, text on the view
     * @param xPic
     */
    private void renderXkcdPic(XkcdPic xPic) {
        tvTitle.setText(xPic.num + ". " + xPic.title);
        Glide.with(this).load(xPic.img).into(ivXkcdPic);
        Log.d("MainActivity", "Pic to be loaded: " + xPic.img);
        tvCreateDate.setText("created on " + xPic.year + "." + xPic.month + "." + xPic.day);
    }
}
