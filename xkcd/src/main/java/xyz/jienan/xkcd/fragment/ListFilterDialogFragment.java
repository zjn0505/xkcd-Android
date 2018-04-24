package xyz.jienan.xkcd.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.jienan.xkcd.R;

public class ListFilterDialogFragment extends DialogFragment {

    private List<HashMap<String, String>> list;
    private int selection;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        selection = sharedPreferences.getInt("FILTER_SELECTION", 0);
        final String[] filter = {getString(R.string.filter_all_comics), getString(R.string.filter_my_fav), getString(R.string.filter_people_choice)};
        final int[] icons = {R.mipmap.ic_launcher_round, R.drawable.ic_heart_on, R.drawable.ic_thumb_on};
        list = new ArrayList<>();
        for (int i = 0; i < filter.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("filter", filter[i]);
            hashMap.put("iconRes", String.valueOf(icons[i]));
            list.add(hashMap);
        }
        FilterAdapter adapter = new FilterAdapter();

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences.edit().putInt("FILTER_SELECTION", which).apply();
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private class FilterAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_filter_dialog, parent, false);
            HashMap<String, String> map = list.get(position);
            ImageView ivFilter = view.findViewById(R.id.iv_filter);
            CheckedTextView tvFilter = view.findViewById(R.id.tv_filter);
            ivFilter.setImageResource(Integer.valueOf(map.get("iconRes")));
            tvFilter.setChecked(position == selection);
            tvFilter.setText(map.get("filter"));
            return view;
        }
    }
}
