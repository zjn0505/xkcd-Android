package xyz.jienan.xkcd.base.network


import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*
import xyz.jienan.xkcd.model.WhatIfArticle

interface WhatIfAPI {

    @get:Headers("$HEADER_CACHEABLE: 600")
    @get:GET("archive/")
    val archive: Observable<ResponseBody>

    @GET("{article_id}/")
    fun getArticle(@Path("article_id") id: Long): Observable<ResponseBody>

    @FormUrlEncoded
    @POST
    fun thumbsUpWhatIf(@Url url: String, @Field("what_if_id") whatIfId: Int): Observable<WhatIfArticle>

    @Headers("$HEADER_CACHEABLE: 60")
    @GET
    fun getTopWhatIfs(@Url url: String, @Query("sortby") sortby: String): Observable<List<WhatIfArticle>>
}
