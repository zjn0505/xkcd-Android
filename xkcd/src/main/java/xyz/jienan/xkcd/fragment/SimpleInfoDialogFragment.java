package xyz.jienan.xkcd.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
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

    private final static String CONTENT = "content";
    private final static String HTML_CONTENT = "html_content";
    private final static String INT_MAX = "max";

    public interface ISimpleInfoDialogListener {
        void onPositiveClick();
        void onNegativeClick();
        void onExplainMoreClick(ExplainingCallback explainingCallback);
    }

    private String content;
    private String htmlContent;
    private ISimpleInfoDialogListener mListener;
    private TextView tvExplain;
    private ProgressBar pbLoading;
    private Button buttonNegative;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            content = savedInstanceState.getString(CONTENT);
            htmlContent = savedInstanceState.getString(HTML_CONTENT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CONTENT, content);
        outState.putString(HTML_CONTENT, htmlContent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_explain, null);
        tvExplain = (TextView) view.findViewById(R.id.tv_explain);
        int negativeBtnTextId = R.string.dialog_more_details;
        if (TextUtils.isEmpty(htmlContent)) {
            tvExplain.setText(content);
        } else {
            tvExplain.setText(Html.fromHtml(htmlContent));
            tvExplain.setMovementMethod(LinkMovementMethod.getInstance());
            negativeBtnTextId = R.string.go_to_explainxkcd;
            hasExplainedMore = true;
        }

        pbLoading = (ProgressBar) view.findViewById(R.id.pb_explaining);
        builder.setView(view)
                .setPositiveButton(R.string.dialog_got_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPositiveClick();
                        dismiss();
                    }
                })
                .setNegativeButton(negativeBtnTextId, null);
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(showListener);
        return dialog;
    }

    private DialogInterface.OnShowListener showListener = new DialogInterface.OnShowListener() {

        @Override
        public void onShow(final DialogInterface dialog) {
            buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            buttonNegative.setOnClickListener(new View.OnClickListener() {
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
                                    htmlContent = result;
                                    tvExplain.setText(Html.fromHtml(result));
                                    tvExplain.setMovementMethod(LinkMovementMethod.getInstance());
                                }
                                buttonNegative.setText(R.string.go_to_explainxkcd);
                                hasExplainedMore = true;
                                isLoading = false;
                            }

                            @Override
                            public void explanationFailed() {
                                if (dialog != null && ((AlertDialog) dialog).isShowing()) {
                                    Toast.makeText(v.getContext(), R.string.toast_more_explain_failed, Toast.LENGTH_SHORT).show();
                                    pbLoading.setVisibility(View.GONE);
                                    buttonNegative.setText(R.string.more_on_explainxkcd);
                                    hasExplainedMore = true;
                                    isLoading = false;
                                }
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
    };
}
