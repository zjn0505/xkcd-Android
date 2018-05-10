package xyz.jienan.xkcd.base.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import timber.log.Timber;
import xyz.jienan.xkcd.BuildConfig;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;

/**
 * Created by Jienan on 2018/3/2.
 */

public class NetworkService {

    public static final String XKCD_SPECIAL_LIST = "https://raw.githubusercontent.com/zjn0505/Xkcd-Android/master/xkcd/src/main/res/raw/xkcd_special.json";
    public static final String XKCD_SEARCH_SUGGESTION = "http://130.211.211.220:3003/xkcd-suggest";
    public static final String XKCD_BROWSE_LIST = "http://130.211.211.220:3003/xkcd-list";
    public static final String XKCD_THUMBS_UP = "http://130.211.211.220:3003/xkcd-thumb-up";
    public static final String XKCD_TOP = "http://130.211.211.220:3003/xkcd-top";
    public static final String XKCD_TOP_SORT_BY_THUMB_UP = "thumb-up";
    public static final String BYPASS_CACHE = "1";
    public static final String USE_CACHE = "2";
    public static final String SHORT_CACHE = "3";
    private static final String XKCD_BASE_URL = "https://xkcd.com/";
    private static final String TAG = "NetworkService";
    private static final int DEFAULT_READ_TIMEOUT = 30; // in seconds
    private static final int DEFAULT_CONNECT_TIMEOUT = 15; // in seconds
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static XkcdAPI xkcdAPI;

    private NetworkService() {
        OkHttpClient client = getOkHttpClientBuilder().build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(XKCD_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        Retrofit retrofit = builder.build();
        xkcdAPI = retrofit.create(XkcdAPI.class);
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
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


        httpClientBuilder = enableTls12(httpClientBuilder)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS);

        return httpClientBuilder;
    }

    /**
     * Enable TLS on the OKHttp builder by setting a custom SocketFactory
     */
    private static OkHttpClient.Builder enableTls12(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return client;
        }
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            client.sslSocketFactory(new TLSSocketFactory(), trustManager);

            ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1)
                    .build();

            List<ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);

            client.connectionSpecs(specs);
        } catch (Exception exc) {
            Timber.e(exc, "Error while setting TLS");
        }
        return client;
    }

    public static XkcdAPI getXkcdAPI() {
        if (xkcdAPI == null) {
            new NetworkService();
        }
        return xkcdAPI;
    }

    public static boolean isNetworkAvailable() {
        Context context = XkcdApplication.getInstance();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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

        @Headers("cacheable: 600")
        @GET("{comic_id}/info.0.json")
        Observable<XkcdPic> getComics(@Path("comic_id") String comicId);

        @Headers("cacheable: 2419200")
        @GET
        Observable<ResponseBody> getExplain(@Url String url);

        @Headers("cacheable: 86400")
        @GET
        Observable<ResponseBody> getExplainWithShortCache(@Url String url);

        @GET
        Observable<List<XkcdPic>> getSpecialXkcds(@Url String url);

        @Headers("cacheable: 600")
        @GET
        Observable<List<XkcdPic>> getXkcdsSearchResult(@Url String url, @Query("q") String query);

        /**
         * Get the xkcd list with paging
         *
         * @param url
         * @param start    the start index of xkcd list
         * @param reversed 0 not reversed, 1 reversed
         * @param size     the size of returned xkcd list
         * @return
         */
        @GET
        Observable<List<XkcdPic>> getXkcdList(@Url String url,
                                              @Query("start") int start, @Query("reversed") int reversed, @Query("size") int size);

        @FormUrlEncoded
        @POST
        Observable<XkcdPic> thumbsUp(@Url String url, @Field("comic_id") int comicId);

        @Headers("cacheable: 60")
        @GET
        Observable<List<XkcdPic>> getTopXkcds(@Url String url, @Query("sortby") String sortby);
    }

    /**
     * Interceptor to cache data and maintain it for four weeks.
     * <p>
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
                        .header(HEADER_CACHE_CONTROL,
                                "public, only-if-cached, max-stale=" + 2419200)
                        .build();
            }
            if ("1".equals(request.header("bypass"))) {
                Request.Builder builder = request.newBuilder().addHeader(HEADER_CACHE_CONTROL, "no-cache");
                request = builder.build();
            }
            return chain.proceed(request);
        }
    }

    /**
     * Interceptor to cache data and maintain it for a minute.
     * <p>
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
                return builder.header(HEADER_CACHE_CONTROL, "no-cache").build();
            } else {
                return builder.header(HEADER_CACHE_CONTROL, "public, max-age=" + cacheable).build();
            }
        }
    }
}