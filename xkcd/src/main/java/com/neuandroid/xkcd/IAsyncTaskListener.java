package com.neuandroid.xkcd;

import java.io.Serializable;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public interface IAsyncTaskListener {
    void onPreExecute();
    void onPostExecute(Serializable result);
}
