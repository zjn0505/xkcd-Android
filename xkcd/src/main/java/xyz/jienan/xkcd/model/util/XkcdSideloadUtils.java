package xyz.jienan.xkcd.model.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.network.NetworkService;
import xyz.jienan.xkcd.model.ExtraComics;
import xyz.jienan.xkcd.model.ExtraModel;
import xyz.jienan.xkcd.model.XkcdPic;

import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_EXTRA_LIST;
import static xyz.jienan.xkcd.base.network.NetworkService.XKCD_SPECIAL_LIST;

/**
 * Created by Jienan on 2018/3/2.
 */

public class XkcdSideloadUtils {

    private static HashMap<Integer, XkcdPic> xkcdSideloadMap = new HashMap<>();

    @SuppressLint("CheckResult")
    public static void init(final Context context) {
        NetworkService.getXkcdAPI()
                .getSpecialXkcds(XKCD_SPECIAL_LIST)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe(ignored -> initXkcdSideloadMap(context))
                .singleOrError()
                .subscribe(xkcdPics -> {
                    for (XkcdPic pic : xkcdPics) {
                        xkcdSideloadMap.put((int) pic.num, pic);
                    }
                }, e -> Timber.e(e, "Failed to init special list"));

        NetworkService.getXkcdAPI()
                .getExtraComics(XKCD_EXTRA_LIST)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe(ignored -> initExtraSideloadMap(context))
                .singleOrError()
                .subscribe(extraComics -> {
                    ExtraModel.getInstance().update(extraComics);
                }, e -> Timber.e(e, "Failed to init special list"));
    }

    public static boolean useLargeImageView(int num) {
        return xkcdSideloadMap.containsKey(num) && xkcdSideloadMap.get(num).large;
    }

    public static boolean isSpecialComics(XkcdPic xkcdPic) {
        return xkcdSideloadMap.containsKey((int) xkcdPic.num);
    }

    public static XkcdPic getPicFromXkcd(XkcdPic xkcdPic) {
        if (xkcdPic.num >= 1084) {
            XkcdPic clone = xkcdPic.clone();
            String img = xkcdPic.getImg();
            int insert = img.indexOf(".png");
            if (insert > 0)
                clone.setImg(img.substring(0, insert) + "_2x" + img.substring(insert, img.length()));
            return clone;
        } else {
            return xkcdPic;
        }
    }

    public static XkcdPic sideload(XkcdPic xkcdPic) {
        XkcdPic clone = getPicFromXkcd(xkcdPic);
        if (isSpecialComics(xkcdPic)) {
            XkcdPic sideload = xkcdSideloadMap.get((int) xkcdPic.num);
            if (sideload.getImg() != null) {
                clone.setImg(sideload.getImg());
            }
            return clone; // special
        }
        return clone; // original or 2x
    }

    private static void initXkcdSideloadMap(Context context) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try (InputStream is = context.getResources().openRawResource(R.raw.xkcd_special)) {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        List<XkcdPic> sideloadList = new Gson().fromJson(writer.toString(), new TypeToken<List<XkcdPic>>() {
        }.getType());
        for (XkcdPic pic : sideloadList) {
            xkcdSideloadMap.put((int) pic.num, pic);
        }
    }

    private static void initExtraSideloadMap(Context context) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try (InputStream is = context.getResources().openRawResource(R.raw.xkcd_extra)) {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        List<ExtraComics> sideloadList = new Gson().fromJson(writer.toString(), new TypeToken<List<ExtraComics>>() {
        }.getType());
        ExtraModel.getInstance().update(sideloadList);
    }
}
