package xyz.jienan.xkcd.base.network

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import xyz.jienan.xkcd.model.ExtraComics
import xyz.jienan.xkcd.model.XkcdPic

interface XkcdAPI {
    @get:GET("info.0.json")
    val latest: Observable<XkcdPic>

    @get:GET(XKCD_SPECIAL_LIST)
    val specialXkcds: Observable<List<XkcdPic>>

    @get:GET(XKCD_EXTRA_LIST)
    val extraComics: Observable<List<ExtraComics>>

    @Headers("$HEADER_CACHEABLE: 600")
    @GET("{comic_id}/info.0.json")
    fun getComics(@Path("comic_id") comicId: Long): Observable<XkcdPic>

    @Headers("$HEADER_CACHEABLE: 2419200")
    @GET
    fun getExplain(@Url url: String): Observable<ResponseBody>

    @GET
    fun getExplainWithShortCache(@Url url: String, @Header(HEADER_CACHEABLE) cache: Long): Observable<ResponseBody>

    @Headers("$HEADER_CACHEABLE: 600")
    @GET(XKCD_SEARCH_SUGGESTION)
    fun getXkcdsSearchResult(@Query("q") query: String): Observable<List<XkcdPic>>

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
    fun getXkcdList(@Url url: String,
                    @Query("start") start: Int, @Query("reversed") reversed: Int, @Query("size") size: Int): Observable<List<XkcdPic>>

    @FormUrlEncoded
    @POST(XKCD_THUMBS_UP)
    fun thumbsUp(@Field("comic_id") comicId: Int): Observable<XkcdPic>

    @Headers("$HEADER_CACHEABLE: 60")
    @GET(XKCD_TOP)
    fun getTopXkcds(@Query("sortby") sortby: String): Observable<List<XkcdPic>>

    @Headers("$HEADER_CACHEABLE: 600")
    @GET
    fun getLocalizedXkcd(@Url url: String): Single<XkcdPic>
}