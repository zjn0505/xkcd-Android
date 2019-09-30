package xyz.jienan.xkcd.base.glide;

import okhttp3.HttpUrl;

/**
 * Created by Jienan on 2018/3/7.
 */

interface ResponseProgressListener {
    void update(HttpUrl url, long bytesRead, long contentLength);
}