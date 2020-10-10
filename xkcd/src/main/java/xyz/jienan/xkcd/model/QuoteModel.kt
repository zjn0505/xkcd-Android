package xyz.jienan.xkcd.model

import android.content.res.Resources
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import xyz.jienan.xkcd.BuildConfig
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.network.NetworkService
import java.util.*

object QuoteModel {

    private val INTERVAL = (if (BuildConfig.DEBUG) 10000 else 1000 * 60 * 60 * 24).toLong()

    fun getQuoteOfTheDay(previousQuote: Quote, resources: Resources): Observable<Quote> =
            Observable.just(previousQuote)
                    .flatMap {
                        if (System.currentTimeMillis() - it.timestamp < INTERVAL) {
                            Observable.just(it)
                        } else {
                            queryNewQuote(it, resources)
                        }
                    }

    private fun queryNewQuote(quote: Quote, resources: Resources) =
            NetworkService.quoteAPI.quotes
                    .subscribeOn(Schedulers.io())
                    .onErrorReturnItem(resources.readRawJson(R.raw.quotes))
                    .map { quotes ->
                        quotes.remove(quote)
                        quotes
                    }
                    .map { quotes -> quotes[Random().nextInt(quotes.size)] }
                    .doOnNext { result -> result.timestamp = System.currentTimeMillis() }
}

private inline fun <reified T> Resources.readRawJson(@RawRes rawResId: Int): T {
    openRawResource(rawResId).bufferedReader().use {
        return Gson().fromJson<T>(it, object: TypeToken<T>() {}.type)
    }
}