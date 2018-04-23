package xyz.jienan.xkcd.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import xyz.jienan.xkcd.R;

/**
 * Created by jienanzhang on 21/03/2018.
 */

public class SearchCursorAdapter extends CursorAdapter {

    public SearchCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public SearchCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public SearchCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_search_suggestion, parent, false);

        String url = cursor.getString(1);
        ImageView ivThumbnail = view.findViewById(R.id.iv_thumbnail);
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(ivThumbnail);

        ((TextView) view.findViewById(R.id.tv_xkcd_title))
                .setText(context.getResources().getString(R.string.item_search_title, cursor.getString(3), cursor.getString(2)));


        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String url = cursor.getString(1);
        ImageView ivThumbnail = view.findViewById(R.id.iv_thumbnail);
        Glide.with(context).load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(ivThumbnail);

        ((TextView) view.findViewById(R.id.tv_xkcd_title))
                .setText(context.getResources().getString(R.string.item_search_title, cursor.getString(3), cursor.getString(2)));
    }
}
