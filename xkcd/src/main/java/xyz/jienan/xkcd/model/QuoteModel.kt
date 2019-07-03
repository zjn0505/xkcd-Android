package xyz.jienan.xkcd.model

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.base.network.NetworkService
import java.util.*

object QuoteModel {

    private val INTERVAL = (if (BuildConfig.DEBUG) 10000 else 1000 * 60 * 60 * 24).toLong()

    fun getQuoteOfTheDay(previousQuote: Quote): Observable<Quote> =
            Observable.just(previousQuote)
                    .flatMap {
                        if (System.currentTimeMillis() - it.timestamp < INTERVAL) {
                            Observable.just(it)
                        } else {
                            queryNewQuote(it)
                        }
                    }

    private fun queryNewQuote(quote: Quote) =
            NetworkService.quoteAPI.quotes
                    .subscribeOn(Schedulers.io())
                    .doOnNext { quotes -> quotes.remove(quote) }
                    .map { quotes -> quotes[Random().nextInt(quotes.size)] }
                    .doOnNext { result -> result.timestamp = System.currentTimeMillis() }
}
