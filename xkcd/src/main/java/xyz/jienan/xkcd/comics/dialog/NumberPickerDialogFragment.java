package xyz.jienan.xkcd.comics.dialog;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import xyz.jienan.xkcd.R;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class NumberPickerDialogFragment extends DialogFragment {

    public final static String TAG = "IdPickerDialogFragment";

    private final static String INT_MIN = "min";
    private final static String INT_MAX = "max";
    private final static String TITLE_RES = "title";
    private int min, max;
    private int title;
    private INumberPickerDialogListener mListener;

    public void setListener(INumberPickerDialogListener listener) {
        mListener = listener;
    }

    public void setNumberRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            max = savedInstanceState.getInt(INT_MAX);
            min = savedInstanceState.getInt(INT_MIN);
            title = savedInstanceState.getInt(TITLE_RES);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INT_MAX, max);
        outState.putInt(INT_MIN, min);
        outState.putInt(TITLE_RES, title);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_picker, null);
        final NumberPicker picker = contentView.findViewById(R.id.picker_xkcd_id);
        final EditText editText = picker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
        picker.setMinValue(min);
        picker.setMaxValue(max);
        builder.setView(contentView)
                .setTitle(title)
                .setPositiveButton(R.string.dialog_select, (dialog, id) -> {
                    int value;
                    if (editText != null && editText.isFocused()) {
                        value = Integer.valueOf(editText.getText().toString());
                    } else {
                        value = picker.getValue();
                    }
                    mListener.onPositiveClick(value);
                    dismiss();
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, id) -> {
                    mListener.onNegativeClick();
                    dismiss();
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public interface INumberPickerDialogListener {
        void onPositiveClick(int number);

        void onNegativeClick();
    }
}
