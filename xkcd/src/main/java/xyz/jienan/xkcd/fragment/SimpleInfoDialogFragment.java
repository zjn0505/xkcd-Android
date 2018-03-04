package xyz.jienan.xkcd.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import xyz.jienan.xkcd.R;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class SimpleInfoDialogFragment extends DialogFragment {

    public interface ISimpleInfoDialogListener {
        void onPositiveClick();
        void onNegativeClick();
        void onExplainMoreClick(ExplainingCallback explainingCallback);
    }

    private String title;
    private String content;
    private ISimpleInfoDialogListener mListener;
    private TextView tvExplain;
    private boolean hasExplainedMore = false;

    public void setListener(ISimpleInfoDialogListener listener) {
        mListener = listener;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public interface ExplainingCallback {
        void explanationLoaded(String result);
        void explanationFailed();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_explain, null);
        tvExplain = (TextView) view.findViewById(R.id.tv_explain);
        tvExplain.setText(content);
        final ProgressBar pbLoading = (ProgressBar) view.findViewById(R.id.pb_explaining);
        builder.setView(view)
                .setPositiveButton(R.string.dialog_got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPositiveClick();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_more_details, null);
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    private boolean isLoading = false;

                    @Override
                    public void onClick(final View v) {
                        if (!hasExplainedMore) {
                            if (isLoading) {
                                return;
                            }
                            mListener.onExplainMoreClick(new ExplainingCallback() {
                                @Override
                                public void explanationLoaded(String result) {
                                    pbLoading.setVisibility(View.GONE);
                                    if (tvExplain != null) {
                                        tvExplain.setText(Html.fromHtml(result));
                                        tvExplain.setMovementMethod(LinkMovementMethod.getInstance());
                                    }
                                    button.setText("GO TO EXPLAINXKCD");
                                    hasExplainedMore = true;
                                    isLoading = false;
                                }

                                @Override
                                public void explanationFailed() {
                                    Toast.makeText(v.getContext(), "Failed to get more details", Toast.LENGTH_SHORT).show();
                                    pbLoading.setVisibility(View.GONE);
                                    button.setText("MORE ON EXPLAINXKCD");
                                    hasExplainedMore = true;
                                    isLoading = false;
                                }
                            });
                            pbLoading.setVisibility(View.VISIBLE);
                            isLoading = true;
                        } else {
                            mListener.onNegativeClick();
                            dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }
}
