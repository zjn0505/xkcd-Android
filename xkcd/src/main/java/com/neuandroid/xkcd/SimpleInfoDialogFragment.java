package com.neuandroid.xkcd;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class SimpleInfoDialogFragment extends DialogFragment {

    public interface ISimpleInfoDialogListener {
        void onPositiveClick();
        void onNegativeClick();
    }

    private String title;
    private String content;
    private ISimpleInfoDialogListener mListener;


    public void setListener(ISimpleInfoDialogListener listener) {
        mListener = listener;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(content)
                .setPositiveButton(R.string.dialog_got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPositiveClick();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_more_details, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onNegativeClick();
                        dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
