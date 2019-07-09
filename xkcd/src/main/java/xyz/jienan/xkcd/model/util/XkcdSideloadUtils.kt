package xyz.jienan.xkcd.model.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.network.NetworkService
import xyz.jienan.xkcd.model.ExtraComics
import xyz.jienan.xkcd.model.ExtraModel
import xyz.jienan.xkcd.model.XkcdPic
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.StringWriter

/**
 * Created by Jienan on 2018/3/2.
 */

object XkcdSideloadUtils {

    private val xkcdSideloadMap = hashMapOf<Int, XkcdPic>()

    fun init(context: Context) {
        loadSpecial(context)
        loadExtra(context)
    }

    fun isSpecialComics(xkcdPic: XkcdPic) = xkcdSideloadMap.containsKey(xkcdPic.num.toInt())

    fun getPicFromXkcd(xkcdPic: XkcdPic): XkcdPic {
        return if (xkcdPic.num >= 1084) {
            val clone = xkcdPic.copy()
            val img = xkcdPic.img
            val insert = img.indexOf(".png")
            if (insert > 0)
                clone.img = img.substring(0, insert) + "_2x" + img.substring(insert, img.length)
            clone
        } else {
            xkcdPic
        }
    }

    fun sideload(xkcdPic: XkcdPic): XkcdPic {
        val clone = getPicFromXkcd(xkcdPic)
        if (isSpecialComics(xkcdPic)) {
            val sideload = xkcdSideloadMap[xkcdPic.num.toInt()]
            if (sideload!!.img != null) {
                clone.img = sideload.img
            }
            return clone // special
        }
        return clone // original or 2x
    }

    @SuppressLint("CheckResult")
    private fun loadSpecial(context: Context) {
        NetworkService.xkcdAPI
                .specialXkcds
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .singleOrError()
                .doOnSubscribe { initXkcdSideloadMap(context) }
                .subscribe({ xkcdPics -> xkcdPics.forEach { xkcdSideloadMap[it.num.toInt()] = it } },
                        { e -> Timber.e(e, "Failed to init special list") })
    }

    @Throws(IOException::class)
    private fun initXkcdSideloadMap(context: Context) {
        val sideloadList = Gson().fromJson<List<XkcdPic>>(
                context.loadFromRaw(R.raw.xkcd_special), object : TypeToken<List<XkcdPic>>() {
        }.type)
        sideloadList.forEach { xkcdSideloadMap[it.num.toInt()] = it }
    }

    @SuppressLint("CheckResult")
    private fun loadExtra(context: Context) {
        NetworkService.xkcdAPI
                .extraComics
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .singleOrError()
                .doOnSubscribe { initExtraSideloadMap(context) }
                .subscribe({ extraComics -> ExtraModel.update(extraComics) },
                        { e -> Timber.e(e, "Failed to init extra list") })
    }

    @Throws(IOException::class)
    private fun initExtraSideloadMap(context: Context) {
        val sideloadList = Gson().fromJson<List<ExtraComics>>(
                context.loadFromRaw(R.raw.xkcd_extra), object : TypeToken<List<ExtraComics>>() {
        }.type)
        ExtraModel.update(sideloadList)
    }

    @Throws(IOException::class)
    private fun Context.loadFromRaw(@RawRes raw: Int): String {
        val writer = StringWriter()
        val buffer = CharArray(1024)
        resources.openRawResource(raw).use {
            val reader = BufferedReader(InputStreamReader(it, "UTF-8"))
            var n = reader.read(buffer)
            while (n != -1) {
                writer.write(buffer, 0, n)
                n = reader.read(buffer)
            }
        }
        return writer.toString()
    }
}
