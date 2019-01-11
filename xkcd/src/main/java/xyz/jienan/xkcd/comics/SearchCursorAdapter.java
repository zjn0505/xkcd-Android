package xyz.jienan.xkcd.comics;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import androidx.cursoradapter.widget.CursorAdapter;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.glide.GlideUtils;

/**
 * Created by jienanzhang on 21/03/2018.
 */

public class SearchCursorAdapter extends CursorAdapter {

    private RequestManager glide;

    public SearchCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        glide = Glide.with(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_search_suggestion, parent, false);

        String url = cursor.getString(1);
        ImageView ivThumbnail = view.findViewById(R.id.iv_thumbnail);
        GlideUtils.load(glide, url, ivThumbnail);

        ((TextView) view.findViewById(R.id.tv_xkcd_title))
                .setText(context.getResources().getString(R.string.item_search_title,
                        cursor.getString(3),
                        cursor.getString(2)));

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String url = cursor.getString(1);
        ImageView ivThumbnail = view.findViewById(R.id.iv_thumbnail);
        GlideUtils.load(glide, url, ivThumbnail);

        ((TextView) view.findViewById(R.id.tv_xkcd_title))
                .setText(context.getResources().getString(R.string.item_search_title,
                        cursor.getString(3),
                        cursor.getString(2)));
    }


}
