package xyz.jienan.xkcd.base.glide;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.jienan.xkcd.base.network.NetworkService;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class OkHttpProgressGlideModule implements GlideModule {
    private static Interceptor createInterceptor(final ResponseProgressListener listener) {
        return chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);
            return response.newBuilder()
                    .body(new OkHttpProgressResponseBody(request.url(), response.body(), listener))
                    .build();
        };
    }

    public static void forget(String url) {
        DispatchingProgressListener.forget(url);
    }

    public static void expect(String url, UIProgressListener listener) {
        DispatchingProgressListener.expect(url, listener);
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        OkHttpClient.Builder httpClientBuilder = NetworkService.getOkHttpClientBuilder();
        httpClientBuilder.addNetworkInterceptor(createInterceptor(new DispatchingProgressListener()));
        OkHttpClient client = httpClientBuilder.build();
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    public interface UIProgressListener {
        void onProgress(long bytesRead, long expectedLength);

        /**
         * Control how often the listener needs an update. 0% and 100% will always be dispatched.
         *
         * @return in percentage (0.2 = call {@link #onProgress} around every 0.2 percent of progress)
         */
        float getGranualityPercentage();

        void onDownloadStart();

        void onProgress(int progress);

        void onDownloadFinish();
    }

    private static class DispatchingProgressListener implements ResponseProgressListener {
        private static final Map<String, UIProgressListener> LISTENERS = new HashMap<>();
        private static final Map<String, Long> PROGRESSES = new HashMap<>();

        private final Handler handler;

        DispatchingProgressListener() {
            this.handler = new Handler(Looper.getMainLooper());
        }

        static void forget(String url) {
            LISTENERS.remove(url);
            PROGRESSES.remove(url);
        }

        static void expect(String url, UIProgressListener listener) {
            LISTENERS.put(url, listener);
        }

        @Override
        public void update(HttpUrl url, final long bytesRead, final long contentLength) {
            //System.out.printf("%s: %d/%d = %.2f%%%n", url, bytesRead, contentLength, (100f * bytesRead) / contentLength);
            String key = url.toString();
            final UIProgressListener listener = LISTENERS.get(key);
            if (listener == null) {
                return;
            }
            Long lastProgress = PROGRESSES.get(key);
            if (lastProgress == null) {
                // ensure `onStart` is called before `onProgress` and `onFinish`
                listener.onDownloadStart();
            }
            if (contentLength <= bytesRead) {
                listener.onDownloadFinish();
                forget(key);
            }
            if (needsDispatch(key, bytesRead, contentLength, listener.getGranualityPercentage())) {
                handler.post(() -> listener.onProgress(bytesRead, contentLength));
            }
        }

        private boolean needsDispatch(String key, long current, long total, float granularity) {
            if (granularity == 0 || current == 0 || total == current) {
                return true;
            }
            float percent = 100f * current / total;
            long currentProgress = (long) (percent / granularity);
            Long lastProgress = PROGRESSES.get(key);
            if (lastProgress == null || currentProgress != lastProgress) {
                PROGRESSES.put(key, currentProgress);
                return true;
            } else {
                return false;
            }
        }
    }
}
