package xyz.jienan.xkcd.home.dialog;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import xyz.jienan.xkcd.R;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class NumberPickerDialogFragment extends DialogFragment {

    private final static String INT_MIN = "min";
    private final static String INT_MAX = "max";
    private int min, max;
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
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INT_MAX, max);
        outState.putInt(INT_MIN, min);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_picker, null);
        final NumberPicker picker = contentView.findViewById(R.id.picker_xkcd_id);
        final EditText editText = picker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
        picker.setMinValue(min);
        picker.setMaxValue(max);
        builder.setView(contentView)
                .setTitle(R.string.dialog_pick_content)
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

    public interface INumberPickerDialogListener {
        void onPositiveClick(int number);

        void onNegativeClick();
    }
}
