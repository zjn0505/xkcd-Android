package xyz.jienan.xkcd.comics.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.model.XkcdPic;
import xyz.jienan.xkcd.model.util.ExplainLinkUtil;
import xyz.jienan.xkcd.ui.CustomMovementMethod;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class SimpleInfoDialogFragment extends DialogFragment {

    private static final String CONTENT = "content";

    private static final String HTML_CONTENT = "html_content";

    @BindView(R.id.tv_explain)
    TextView tvExplain;

    @BindView(R.id.pb_explaining)
    ProgressBar pbLoading;

    private String xkcdContent;

    private String htmlContent;

    private ISimpleInfoDialogListener mListener;

    private Button buttonNegative;

    private boolean hasExplainedMore = false;

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
                                    ExplainLinkUtil.setTextViewHTML(tvExplain, result);
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

    public void setListener(ISimpleInfoDialogListener listener) {
        mListener = listener;
    }

    public void setPic(XkcdPic pic) {
        this.xkcdContent = pic.getAlt();
    }

    public void setExtraExplain(String string) {
        pbLoading.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(string)) {
            ExplainLinkUtil.setTextViewHTML(tvExplain, string);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            xkcdContent = savedInstanceState.getString(CONTENT);
            htmlContent = savedInstanceState.getString(HTML_CONTENT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CONTENT, xkcdContent);
        outState.putString(HTML_CONTENT, htmlContent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_explain, null);
        ButterKnife.bind(this, view);
        int negativeBtnTextId = R.string.dialog_more_details;
        if (TextUtils.isEmpty(htmlContent) && TextUtils.isEmpty(xkcdContent)) {
            // For extra comics
            pbLoading.setVisibility(View.VISIBLE);
            negativeBtnTextId = R.string.go_to_explainxkcd;
            hasExplainedMore = true;
        } else if (TextUtils.isEmpty(htmlContent)) {
            tvExplain.setText(xkcdContent);
        } else {
            tvExplain.setText(Html.fromHtml(htmlContent));
            tvExplain.setMovementMethod(CustomMovementMethod.getInstance());
            negativeBtnTextId = R.string.go_to_explainxkcd;
            hasExplainedMore = true;
        }
        builder.setView(view)
                .setPositiveButton(R.string.dialog_got_it, (dialog, id) -> {
                    mListener.onPositiveClick();
                    dismiss();
                })
                .setNegativeButton(negativeBtnTextId, null);
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(showListener);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        mListener = null;
        super.onDestroyView();
    }

    public interface ISimpleInfoDialogListener {
        void onPositiveClick();

        void onNegativeClick();

        void onExplainMoreClick(ExplainingCallback explainingCallback);
    }

    public interface ExplainingCallback {
        void explanationLoaded(String result);

        void explanationFailed();
    }
}
