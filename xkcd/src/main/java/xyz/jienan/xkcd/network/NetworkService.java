package xyz.jienan.xkcd.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Url;
import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;

/**
 * Created by Jienan on 2018/3/2.
 */

public class NetworkService {

    private static final String XKCD_BASE_URL = "https://xkcd.com/";
    public static final String XKCD_QUERY_BASE_URL = "https://xkcd.com/info.0.json";
    public static final String XKCD_QUERY_BY_ID_URL = "https://xkcd.com/%s/info.0.json";
    public static final String XKCD_SPECIAL_LIST  = "https://raw.githubusercontent.com/zjn0505/Xkcd-Android/master/xkcd/src/main/res/raw/xkcd_special.json";

    public static final String BYPASS_CACHE = "1";
    public static final String USE_CACHE = "2";
    public static final String SHORT_CACHE = "3";


    private static XkcdAPI xkcdAPI;

    private NetworkService() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addNetworkInterceptor(new NetworkCacheInterceptor())
                .addInterceptor(new ApplicationCacheInterceptor());

        //setup cache
        File httpCacheDirectory = new File(XkcdApplication.getInstance().getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        //add cache to the client
        httpClientBuilder.cache(cache);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            httpClientBuilder.addInterceptor(interceptor);
        }

        OkHttpClient client = httpClientBuilder.build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(XKCD_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        Retrofit retrofit = builder.build();
        xkcdAPI = retrofit.create(XkcdAPI.class);
    }


    /**
     * Interceptor to cache data and maintain it for four weeks.
     *
     * If the device is offline, stale (at most four weeks old)
     * response is fetched from the cache.
     */
    private static class ApplicationCacheInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!isNetworkAvailable()) {
                request = request.newBuilder()
                        .removeHeader("pragma")
                        .header("Cache-Control",
                                "public, only-if-cached, max-stale=" + 2419200)
                        .build();
            }
            if ("1".equals(request.header("bypass"))) {
                Request.Builder builder = request.newBuilder().addHeader("Cache-Control", "no-cache");
                request =  builder.build();
            }
            return chain.proceed(request);
        }
    }

    /**
     * Interceptor to cache data and maintain it for a minute.
     *
     * If the same network request is sent within a minute,
     * the response is retrieved from cache.
     */
    private static class NetworkCacheInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String cacheable = request.header("cacheable");
            okhttp3.Response originalResponse = chain.proceed(request);
            Response.Builder builder = originalResponse.newBuilder().removeHeader("pragma").removeHeader("cacheable");
            if (TextUtils.isEmpty(cacheable)) {
                return builder.header("Cache-Control", "no-cache" ).build();
            } else {
                return builder.header("Cache-Control", "public, max-age=" + cacheable).build();
            }
        }
    }


    public static XkcdAPI getXkcdAPI() {
        if (xkcdAPI == null) {
            new NetworkService();
        }
        return xkcdAPI;
    }


    public static boolean isNetworkAvailable() {
        Context context = XkcdApplication.getInstance();
        ConnectivityManager connectivity =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public interface XkcdAPI {

        @GET("info.0.json")
        Observable<XkcdPic> getLatest();

        @Headers("cacheable: 2419200")
        @GET("{comic_id}/info.0.json")
        Observable<XkcdPic> getComics(@Path("comic_id") String comicId);

        @Headers("cacheable: 2419200")
        @GET
        Call<ResponseBody> getExplain(@Url String url);

        @Headers("cacheable: 86400")
        @GET
        Call<ResponseBody> getExplainWithShortCache(@Url String url);

        @GET
        Observable<List<XkcdPic>> getSpecialXkcds(@Url String url);
    }
}
