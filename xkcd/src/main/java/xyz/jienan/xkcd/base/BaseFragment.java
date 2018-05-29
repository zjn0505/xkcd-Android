package xyz.jienan.xkcd.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import static xyz.jienan.xkcd.Const.FIRE_UX_ACTION;

public class BaseFragment extends Fragment {


    protected FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }

    protected void logUXEvent(String event) {
        logUXEvent(event, null);
    }

    protected void logUXEvent(String event, final Map<String, String> params) {
        Bundle bundle = new Bundle();
        bundle.putString(FIRE_UX_ACTION, event);
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (value.matches("-?\\d+")) {
                    bundle.putInt(key, Integer.valueOf(value));
                } else {
                    bundle.putString(key, params.get(key));
                }
            }
        }
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, bundle);
    }
}
