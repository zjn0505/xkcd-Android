package xyz.jienan.xkcd.base.network


import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import xyz.jienan.xkcd.model.WhatIfArticle

interface WhatIfAPI {

    @get:Headers("$HEADER_CACHEABLE: 600")
    @get:GET("archive/")
    val archive: Observable<ResponseBody>


    @Headers("$HEADER_CACHEABLE: 2419200")
    @GET("{article_id}/")
    fun getArticle(@Path("article_id") id: Long): Observable<ResponseBody>

    @FormUrlEncoded
    @POST
    fun thumbsUpWhatIf(@Url url: String, @Field("what_if_id") whatIfId: Int): Observable<WhatIfArticle>

    @Headers("$HEADER_CACHEABLE: 60")
    @GET
    fun getTopWhatIfs(@Url url: String, @Query("sortby") sortby: String): Observable<List<WhatIfArticle>>
}
