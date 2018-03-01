package com.neuandroid.xkcd;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

public class XkcdQueryTask extends AsyncTask<URL, Object, String> {

    public interface IAsyncTaskListener {
        void onPreExecute();
        void onPostExecute(Serializable result);
    }

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
        XkcdPic xPic = new Gson().fromJson(result, XkcdPic.class);
        xPic.img = transformXkcdImgUrl(xPic);
        listener.onPostExecute(xPic);
    }

    private String transformXkcdImgUrl(XkcdPic xPic) {
        if (xPic != null && xPic.num >= 1084) {
            String img = xPic.img;
            int insert = img.indexOf(".png");
            return img.substring(0, insert) + "_2x" + img.substring(insert, img.length());
        }
        return xPic == null ? null : xPic.img;
    }

}
