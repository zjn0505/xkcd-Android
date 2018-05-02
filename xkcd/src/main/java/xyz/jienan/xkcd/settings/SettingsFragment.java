package xyz.jienan.xkcd.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import xyz.jienan.xkcd.R;

import static xyz.jienan.xkcd.Const.PREF_ARROW;
import static xyz.jienan.xkcd.Const.PREF_FONT;

/**
 * Created by Jienan on 2018/3/9.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static boolean needRecreateForParent = false;
    private ListPreference arrowPref;
    private SwitchPreference fontPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        arrowPref = (ListPreference) findPreference(PREF_ARROW);
        fontPref = (SwitchPreference) findPreference(PREF_FONT);
        arrowPref.setSummary(getResources().getQuantityString(R.plurals.pref_arrow_summary,
                Integer.valueOf(arrowPref.getEntry().toString()), arrowPref.getEntry().toString()));
        arrowPref.setOnPreferenceChangeListener(this);
        fontPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PREF_ARROW:
                ((ListPreference) preference).setValue(newValue.toString());
                arrowPref.setSummary(getResources().getQuantityString(R.plurals.pref_arrow_summary,
                        Integer.valueOf(arrowPref.getEntry().toString()), arrowPref.getEntry().toString()));
                break;
            case PREF_FONT:
                ((SwitchPreference) preference).setChecked((boolean) newValue);
                getActivity().recreate();
                needRecreateForParent = true;
                break;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        if (needRecreateForParent) {
            getActivity().setResult(Activity.RESULT_OK);
        }
        super.onDestroyView();
    }
}
