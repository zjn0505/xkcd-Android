package xyz.jienan.xkcd.glide;

import okhttp3.HttpUrl;

/**
 * Created by Jienan on 2018/3/7.
 */

public interface ResponseProgressListener {
    void update(HttpUrl url, long bytesRead, long contentLength);
}