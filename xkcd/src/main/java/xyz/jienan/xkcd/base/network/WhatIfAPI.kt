package xyz.jienan.xkcd.base.network


import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*
import xyz.jienan.xkcd.model.WhatIfArticle

interface WhatIfAPI {

    @get:Headers("$HEADER_CACHEABLE: 600")
    @get:GET("archive/")
    val archive: Single<ResponseBody>

    @GET("{article_id}/")
    fun getArticle(@Path("article_id") id: Long): Single<ResponseBody>

    @FormUrlEncoded
    @POST
    fun thumbsUpWhatIf(@Url url: String, @Field("what_if_id") whatIfId: Int): Single<WhatIfArticle>

    @Headers("$HEADER_CACHEABLE: 60")
    @GET
    fun getTopWhatIfs(@Url url: String, @Query("sortby") sortby: String): Single<List<WhatIfArticle>>
}
