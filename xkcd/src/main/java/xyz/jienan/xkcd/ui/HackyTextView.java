package xyz.jienan.xkcd.ui;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;

public class HackyTextView extends AppCompatTextView {

    public HackyTextView(Context context) {
        super(context);
    }

    public HackyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HackyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (selStart == -1 || selEnd == -1) {
            // @hack : https://issuetracker.google.com/issues/37020604
            CharSequence text = getText();
            if (text instanceof Spannable) {
                Selection.setSelection((Spannable) text, 0, 0);
            }
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }
}
