package xyz.jienan.xkcd.model.util

import android.annotation.SuppressLint
import android.content.Context
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
import java.util.*

/**
 * Created by Jienan on 2018/3/2.
 */

object XkcdSideloadUtils {

    private val xkcdSideloadMap = HashMap<Int, XkcdPic>()

    @SuppressLint("CheckResult")
    fun init(context: Context) {
        NetworkService.xkcdAPI
                .specialXkcds
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { initXkcdSideloadMap(context) }
                .singleOrError()
                .subscribe({ xkcdPics ->
                    for (pic in xkcdPics) {
                        xkcdSideloadMap[pic.num.toInt()] = pic
                    }
                }, { e -> Timber.e(e, "Failed to init special list") })

        NetworkService.xkcdAPI
                .extraComics
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { initExtraSideloadMap(context) }
                .singleOrError()
                .subscribe({ extraComics -> ExtraModel.getInstance().update(extraComics) },
                        { e -> Timber.e(e, "Failed to init special list") })
    }

    fun isSpecialComics(xkcdPic: XkcdPic): Boolean {
        return xkcdSideloadMap.containsKey(xkcdPic.num.toInt())
    }

    fun getPicFromXkcd(xkcdPic: XkcdPic): XkcdPic {
        return if (xkcdPic.num >= 1084) {
            val clone = xkcdPic.clone()
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

    @Throws(IOException::class)
    private fun initXkcdSideloadMap(context: Context) {
        val writer = StringWriter()
        val buffer = CharArray(1024)
        context.resources.openRawResource(R.raw.xkcd_special).use { `is` ->
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var n = reader.read(buffer)
            while (n != -1) {
                writer.write(buffer, 0, n)
                n = reader.read(buffer)
            }
        }
        val sideloadList = Gson().fromJson<List<XkcdPic>>(writer.toString(), object : TypeToken<List<XkcdPic>>() {

        }.type)
        for (pic in sideloadList) {
            xkcdSideloadMap[pic.num.toInt()] = pic
        }
    }

    @Throws(IOException::class)
    private fun initExtraSideloadMap(context: Context) {
        val writer = StringWriter()
        val buffer = CharArray(1024)
        context.resources.openRawResource(R.raw.xkcd_extra).use { `is` ->
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var n = reader.read(buffer)
            while (n != -1) {
                writer.write(buffer, 0, n)
                n = reader.read(buffer)
            }
        }
        val sideloadList = Gson().fromJson<List<ExtraComics>>(writer.toString(), object : TypeToken<List<ExtraComics>>() {

        }.type)
        ExtraModel.getInstance().update(sideloadList)
    }
}
