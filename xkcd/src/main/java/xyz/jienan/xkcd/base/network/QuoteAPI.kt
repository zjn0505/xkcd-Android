package xyz.jienan.xkcd.base.network


import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import xyz.jienan.xkcd.model.Quote

interface QuoteAPI {

    @get:Headers("$HEADER_CACHEABLE: 86400")
    @get:GET(QUOTE_LIST)
    val quotes: Observable<MutableList<Quote>>
}
