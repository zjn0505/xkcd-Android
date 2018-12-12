package xyz.jienan.xkcd.list;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
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
    private OnItemSelectListener itemSelectListener;
    private int[] filters;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final int[] icons = {R.mipmap.ic_launcher_round, R.drawable.ic_heart_on, R.drawable.ic_thumb_on};
        list = new ArrayList<>();
        for (int i = 0; i < filters.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("filter", getString(filters[i]));
            hashMap.put("iconRes", String.valueOf(icons[i]));
            list.add(hashMap);
        }
        FilterAdapter adapter = new FilterAdapter();

        builder.setAdapter(adapter, (dialog, which) -> {
            if (itemSelectListener != null) {
                itemSelectListener.onItemSelected(which);
            }
            dialog.dismiss();
        });

        return builder.create();
    }

    public void setItemSelectListener(OnItemSelectListener itemSelectListener) {
        this.itemSelectListener = itemSelectListener;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }

    public void setFilters(int[] filters) {
        this.filters = filters;
    }

    public interface OnItemSelectListener {
        void onItemSelected(int which);
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
