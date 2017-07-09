package com.neuandroid.xkcd;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

/**
 * This class extends AsyncTask to execute the query out of the main thread.
 * We should always not run a time consuming task on main thread.
 */
class XkcdQueryTask extends AsyncTask<URL, Object, String> {

    private IAsyncTaskListener listener;

    public XkcdQueryTask(IAsyncTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onPreExecute();
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
        if (!TextUtils.isEmpty(result)) {

            try {
                XkcdPic xkcdResult = extractXkcdPicFromJson(result);
                listener.onPostExecute(xkcdResult);
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
