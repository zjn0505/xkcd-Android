package xyz.jienan.xkcd.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.objectbox.Box;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdApplication;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.activity.ImageDetailPageActivity;

/**
 * Created by jienanzhang on 09/07/2017.
 */

public class SimpleInfoDialogFragment extends DialogFragment {

    private final static String CONTENT = "content";
    private final static String HTML_CONTENT = "html_content";

    public interface ISimpleInfoDialogListener {
        void onPositiveClick();
        void onNegativeClick();
        void onExplainMoreClick(ExplainingCallback explainingCallback);
    }

    private Box<XkcdPic> box;

    private String xkcdContent;
    private String htmlContent;
    private ISimpleInfoDialogListener mListener;
    private TextView tvExplain;
    private ProgressBar pbLoading;
    private Button buttonNegative;
    private boolean hasExplainedMore = false;

    public void setListener(ISimpleInfoDialogListener listener) {
        mListener = listener;
    }

    public void setPic(XkcdPic pic) {
        this.xkcdContent = pic.alt;
    }

    public interface ExplainingCallback {
        void explanationLoaded(String result);
        void explanationFailed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            xkcdContent = savedInstanceState.getString(CONTENT);
            htmlContent = savedInstanceState.getString(HTML_CONTENT);
        }
        box = ((XkcdApplication)getActivity().getApplication()).getBoxStore().boxFor(XkcdPic.class);
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
        tvExplain = (TextView) view.findViewById(R.id.tv_explain);
        int negativeBtnTextId = R.string.dialog_more_details;
        if (TextUtils.isEmpty(htmlContent)) {
            tvExplain.setText(xkcdContent);
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
                                    setTextViewHTML(tvExplain, result);
//                                    tvExplain.setText(Html.fromHtml(result));
//                                    tvExplain.setMovementMethod(LinkMovementMethod.getInstance());
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

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                String url = span.getURL();
                if (isXkcdImageLink(url)) {
                    final long id = getXkcdIdFromImageLink(url);
                    XkcdPic xkcdPic = box.get(id);
                    Intent intent = new Intent(getActivity(), ImageDetailPageActivity.class);
                    if (xkcdPic != null) {
                        intent.putExtra("URL", xkcdPic.getTargetImg());
                    }
                    intent.putExtra("ID", id);
                    startActivity(intent);
//                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }

            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void setTextViewHTML(TextView text, String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isXkcdImageLink(String url) {
        final String regex = "^https?://www\\.explainxkcd\\.com/wiki/index\\.php/\\d+.*$";
        return url.matches(regex);
    }

    private long getXkcdIdFromImageLink(String url) {
        final String regex = "^https?://www\\.explainxkcd\\.com/wiki/index\\.php/(\\d+).*$";
        Matcher matcher = Pattern.compile(regex).matcher(url);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }
}
