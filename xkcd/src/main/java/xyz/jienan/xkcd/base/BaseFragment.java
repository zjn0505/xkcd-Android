package xyz.jienan.xkcd.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static xyz.jienan.xkcd.Const.FIRE_UX_ACTION;

public abstract class BaseFragment extends Fragment {

    protected FirebaseAnalytics mFirebaseAnalytics;

    protected Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }

    @LayoutRes
    protected abstract int getLayoutResId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(getLayoutResId(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
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

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }
}
