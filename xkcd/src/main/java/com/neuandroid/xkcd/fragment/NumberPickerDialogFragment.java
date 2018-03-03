package com.neuandroid.xkcd.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.neuandroid.xkcd.R;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class NumberPickerDialogFragment extends DialogFragment {

    public interface INumberPickerDialogListener {
        void onPositiveClick(int number);
        void onNegativeClick();
    }

    private String title, content;
    private int min, max;
    private INumberPickerDialogListener mListener;



    public void setListener(INumberPickerDialogListener listener) {
        mListener = listener;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNumberRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_picker, null);
        final NumberPicker picker = (NumberPicker) contentView.findViewById(R.id.picker_xkcd_id);
        final EditText editText = (EditText) picker.findViewById(Resources.getSystem().getIdentifier("numberpicker_input", "id", "android"));
        picker.setMinValue(min);
        picker.setMaxValue(max);
        builder.setView(contentView)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.dialog_select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int value = 1;
                        if (editText != null && editText.isFocused()) {
                            value = Integer.valueOf(editText.getText().toString());
                        } else {
                            value = picker.getValue();
                        }
                        mListener.onPositiveClick(value);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNegativeClick();
                        dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
