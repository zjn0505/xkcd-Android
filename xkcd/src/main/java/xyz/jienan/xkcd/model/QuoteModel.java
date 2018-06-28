package xyz.jienan.xkcd.model;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;
import xyz.jienan.xkcd.base.network.QuoteAPI;

public class QuoteModel {

    private static QuoteModel quoteModel;

    private final QuoteAPI quoteAPI = NetworkService.getQuoteAPI();

    private QuoteModel() {
        // no public constructor
    }

    public static QuoteModel getInstance() {
        if (quoteModel == null) {
            quoteModel = new QuoteModel();
        }
        return quoteModel;
    }

    public Observable<Quote> getQuoteOfTheDay(final Quote quote) {
        return Observable.just(1)
                .flatMap(ignored -> {
                    if (System.currentTimeMillis() - quote.getTimestamp() < 1000 * 60 * 60 * 24) {
                        return Observable.just(quote);
                    } else {
                        return queryNewQuote(quote);
                    }
                });
    }

    private Observable<Quote> queryNewQuote(final Quote quote) {
        return quoteAPI.getQuotes()
                .subscribeOn(Schedulers.io())
                .doOnNext(quotes -> quotes.remove(quote))
                .map(quotes -> quotes.get(new Random().nextInt(quotes.size())))
                .doOnNext(result -> result.setTimestamp(System.currentTimeMillis()));
    }
}
