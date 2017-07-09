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
        listener.onPostExecute(xPic);
    }

}
