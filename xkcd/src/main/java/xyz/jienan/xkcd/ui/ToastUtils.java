package xyz.jienan.xkcd.ui;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {

    private static Toast toast;

    public static void showToast(Context context, String text) {
        try{
            toast.getView().isShown();
            toast.setText(text);
        } catch (Exception e) {
            toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void cancelToast() {
        if (toast != null && toast.getView().isShown()) {
            toast.cancel();
        }
    }
}