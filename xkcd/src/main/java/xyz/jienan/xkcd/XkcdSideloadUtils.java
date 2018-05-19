package xyz.jienan.xkcd;

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
import xyz.jienan.xkcd.base.network.NetworkService;

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
    }

    public static boolean useLargeImageView(int num) {
        return xkcdSideloadMap.containsKey(num) && xkcdSideloadMap.get(num).large;
    }

    public static XkcdPic sideload(XkcdPic xkcdPic) {
        XkcdPic clone = xkcdPic.clone();
        if (xkcdPic.num >= 1084) {
            String img = xkcdPic.getImg();
            int insert = img.indexOf(".png");
            if (insert > 0)
                clone.setImg(img.substring(0, insert) + "_2x" + img.substring(insert, img.length()));
        }
        if (xkcdSideloadMap.containsKey((int) xkcdPic.num)) {
            XkcdPic sideload = xkcdSideloadMap.get((int) xkcdPic.num);
            if (sideload.getImg() != null) {
                clone.setImg(sideload.getImg());
            }
            if (sideload.getRawTitle() != null) {
                clone.setTitle(sideload.getRawTitle());
            }
            return clone;
        }
        if (xkcdPic.num >= 1084) {
            return clone;
        }
        return xkcdPic;
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

}
