package xyz.jienan.xkcd.base.network;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface WhatIfAPI {

    @Headers("cacheable: 600")
    @GET("archive/")
    Observable<ResponseBody> getArchive();


    @Headers("cacheable: 2419200")
    @GET("{article_id}/")
    Observable<ResponseBody> getArticle(@Path("article_id") long id);

}
