package xyz.jienan.xkcd.base.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.TextUtils
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.XkcdApplication
import java.io.File
import java.io.IOException
import java.security.KeyStore
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by Jienan on 2018/3/2.
 */

object NetworkService {

    val xkcdAPI: XkcdAPI

    val whatIfAPI: WhatIfAPI

    val quoteAPI: QuoteAPI

    val okHttpClientBuilder: OkHttpClient.Builder

    init {
        val httpClientBuilder = OkHttpClient.Builder()
                .addNetworkInterceptor(NetworkCacheInterceptor())
                .addInterceptor(ApplicationCacheInterceptor())
                .addNetworkInterceptor(SelfHostInterceptor())
                .cache(Cache(File(XkcdApplication.instance!!.cacheDir, "responses"), 10 * 1024 * 1024L))

        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        }

        okHttpClientBuilder = enableTls12(httpClientBuilder)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT.toLong(), TimeUnit.SECONDS)

        val client = okHttpClientBuilder.build()

        val builder = Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        xkcdAPI = builder.baseUrl(XKCD_BASE_URL).build().create(XkcdAPI::class.java)
        whatIfAPI = builder.baseUrl(WHAT_IF_BASE_URL).build().create(WhatIfAPI::class.java)
        quoteAPI = builder.build().create(QuoteAPI::class.java)
    }

    /**
     * Interceptor to cache data and maintain it for four weeks.
     *
     *
     * If the device is offline, stale (at most four weeks old)
     * response is fetched from the cache.
     */
    private class ApplicationCacheInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            if (!isNetworkAvailable) {
                request = request.newBuilder()
                        .removeHeader("pragma")
                        .header(HEADER_CACHE_CONTROL,
                                "public, only-if-cached, max-stale=" + 2419200)
                        .build()
            }
            return chain.proceed(request)
        }
    }

    /**
     * Interceptor to cache data and maintain it for a minute.
     *
     *
     * If the same network request is sent within a minute,
     * the response is retrieved from cache.
     */
    private class NetworkCacheInterceptor : Interceptor {

        private val noCacheHeader = CacheControl.Builder().noCache().noStore().build().toString()

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val cacheable = request.header(HEADER_CACHEABLE)
            val originalResponse = chain.proceed(request)
            val builder = originalResponse.newBuilder().removeHeader("pragma").removeHeader(HEADER_CACHEABLE)
            return if (TextUtils.isEmpty(cacheable)) {
                builder.header(HEADER_CACHE_CONTROL, noCacheHeader).build()
            } else {
                builder.header(HEADER_CACHE_CONTROL, "public, max-age=" + cacheable!!).build()
            }
        }
    }

    /**
     * Interceptor to add headers to requests to self hosted endpoints
     */
    private class SelfHostInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            return if (SELF_HOST_ENDPOINTS.contains(request.url().host())) {
                chain.proceed(request.newBuilder().addHeader("x-api-client", HEADER_VAL_CLIENT).build())
            } else {
                chain.proceed(request)
            }
        }
    }

    /**
     * Enable TLS on the OKHttp builder by setting a custom SocketFactory
     */
    private fun enableTls12(client: OkHttpClient.Builder): OkHttpClient.Builder {
        @SuppressLint("ObsoleteSdkInt")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return client
        }
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + Arrays.toString(trustManagers)
            }
            val trustManager = trustManagers[0] as X509TrustManager

            client.sslSocketFactory(TLSSocketFactory(), trustManager)

            val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
                    .build()

            val specs = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                listOf(tlsSpec, ConnectionSpec.CLEARTEXT)
            } else {
                listOf(tlsSpec)
            }

            client.connectionSpecs(specs)
        } catch (exc: Exception) {
            Timber.e(exc, "Error while setting TLS")
        }

        return client
    }

    private val isNetworkAvailable: Boolean
        get() {
            val connectivity = XkcdApplication.instance?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                    ?: return false

            connectivity.allNetworkInfo?.forEach {
                if (it.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }

            return false
        }
}

const val XKCD_SPECIAL_LIST = "https://zjn0505.github.io/xkcd-Android/xkcd_special.json"
const val XKCD_EXTRA_LIST = "https://zjn0505.github.io/xkcd-Android/xkcd_extra.json"
const val QUOTE_LIST = "https://zjn0505.github.io/xkcd-Android/quotes.json"
const val XKCD_SEARCH_SUGGESTION = "https://api.jienan.xyz/xkcd/xkcd-suggest"
const val XKCD_BROWSE_LIST = "https://api.jienan.xyz/xkcd/xkcd-list"
const val XKCD_THUMBS_UP = "https://api.jienan.xyz/xkcd/xkcd-thumb-up"
const val XKCD_TOP = "https://api.jienan.xyz/xkcd/xkcd-top"
const val WHAT_IF_THUMBS_UP = "https://api.jienan.xyz/xkcd/what-if-thumb-up"
const val WHAT_IF_TOP = "https://api.jienan.xyz/xkcd/what-if-top"
const val XKCD_EXPLAIN_URL = "https://www.explainxkcd.com/wiki/index.php/"
const val XKCD_BASE_URL = "https://xkcd.com/"
const val XKCD_TOP_SORT_BY_THUMB_UP = "thumb-up"
const val HEADER_VAL_CLIENT = "xkcd-Android-${BuildConfig.FLAVOR}-${BuildConfig.VERSION_NAME}"
const val HEADER_CACHEABLE = "cacheable"

private const val WHAT_IF_BASE_URL = "https://what-if.xkcd.com/"
private const val DEFAULT_READ_TIMEOUT = 30 // in seconds
private const val DEFAULT_CONNECT_TIMEOUT = 15 // in seconds
private const val HEADER_CACHE_CONTROL = "Cache-Control"
private val SELF_HOST_ENDPOINTS = listOf("api.jienan.xyz", "xkcd.jienan.xyz")

