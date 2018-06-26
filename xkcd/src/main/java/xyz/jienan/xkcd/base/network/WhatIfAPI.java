package xyz.jienan.xkcd.base.network;


import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import xyz.jienan.xkcd.model.WhatIfArticle;

public interface WhatIfAPI {

    @Headers("cacheable: 600")
    @GET("archive/")
    Observable<ResponseBody> getArchive();


    @Headers("cacheable: 2419200")
    @GET("{article_id}/")
    Observable<ResponseBody> getArticle(@Path("article_id") long id);

    @FormUrlEncoded
    @POST
    Observable<WhatIfArticle> thumbsUpWhatIf(@Url String url, @Field("what_if_id") int whatIfId);

    @Headers("cacheable: 60")
    @GET
    Observable<List<WhatIfArticle>> getTopWhatIfs(@Url String url, @Query("sortby") String sortby);
}
