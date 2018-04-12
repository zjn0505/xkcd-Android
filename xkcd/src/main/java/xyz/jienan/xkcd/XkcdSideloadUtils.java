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

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import xyz.jienan.xkcd.network.NetworkService;

import static xyz.jienan.xkcd.network.NetworkService.XKCD_SPECIAL_LIST;

/**
 * Created by Jienan on 2018/3/2.
 */

public class XkcdSideloadUtils {

    private static HashMap<Integer, XkcdPic> xkcdSideloadMap = new HashMap<>();

    @SuppressLint("CheckResult")
    public static void init(final Context context) {
        Observable.empty().subscribeOn(Schedulers.io()).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                try {
                    initXkcdSideloadMap(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        checkRemoteJson();
    }

    public static boolean useLargeImageView(int num) {
        if (xkcdSideloadMap.containsKey(num)) {
            return xkcdSideloadMap.get(num).large;
        }
        return false;
    }

    private static void checkRemoteJson() {
        NetworkService.getXkcdAPI()
                .getSpecialXkcds(XKCD_SPECIAL_LIST).observeOn(Schedulers.io()).subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<XkcdPic>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<XkcdPic> xkcdPics) {
                        for (XkcdPic pic : xkcdPics) {
                            xkcdSideloadMap.put((int) pic.num, pic);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Failed to get remote special list");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
        InputStream is = context.getResources().openRawResource(R.raw.xkcd_special);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        List<XkcdPic> sideloadList = new Gson().fromJson(writer.toString(), new TypeToken<List<XkcdPic>>() {
        }.getType());
        for (XkcdPic pic : sideloadList) {
            xkcdSideloadMap.put((int) pic.num, pic);
        }
    }

}
