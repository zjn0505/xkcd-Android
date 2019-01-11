package xyz.jienan.xkcd.base.network;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.XkcdPic;

public interface XkcdAPI {
    @GET("info.0.json")
    Observable<XkcdPic> getLatest();

    @Headers("cacheable: 600")
    @GET("{comic_id}/info.0.json")
    Observable<XkcdPic> getComics(@Path("comic_id") long comicId);

    @Headers("cacheable: 2419200")
    @GET
    Observable<ResponseBody> getExplain(@Url String url);

    @GET
    Observable<ResponseBody> getExplainWithShortCache(@Url String url, @Header("cacheable") long cache);

    @GET
    Observable<List<XkcdPic>> getSpecialXkcds(@Url String url);

    @GET
    Observable<List<ExtraComics>> getExtraComics(@Url String url);

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
