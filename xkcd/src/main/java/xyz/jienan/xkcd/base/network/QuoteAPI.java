package xyz.jienan.xkcd.base.network;


import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import xyz.jienan.xkcd.model.Quote;

import static xyz.jienan.xkcd.base.network.NetworkService.QUOTE_LIST;

public interface QuoteAPI {

    @Headers("cacheable: 86400")
    @GET(QUOTE_LIST)
    Observable<List<Quote>> getQuotes();
}
