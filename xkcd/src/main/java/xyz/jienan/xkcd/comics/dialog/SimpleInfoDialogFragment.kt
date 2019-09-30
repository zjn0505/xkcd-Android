package xyz.jienan.xkcd.comics.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.util.ExplainLinkUtil
import xyz.jienan.xkcd.ui.CustomMovementMethod

/**
 * Created by jienanzhang on 09/07/2017.
 */

class SimpleInfoDialogFragment : DialogFragment() {

    private lateinit var pbLoading: ProgressBar

    private lateinit var tvExplain: TextView

    private var xkcdContent: String? = null

    private var htmlContent: String? = null

    private var mListener: ISimpleInfoDialogListener? = null

    private var hasExplainedMore = false

    private val showListener = DialogInterface.OnShowListener { dialog ->
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)?.let {
            it.setOnClickListener(object : View.OnClickListener {
                private var isLoading = false

                override fun onClick(v: View) {
                    if (!hasExplainedMore) {
                        if (isLoading) {
                            return
                        }
                        mListener?.onExplainMoreClick(object : ExplainingCallback {
                            override fun explanationLoaded(result: String) {
                                pbLoading.visibility = View.GONE
                                if (tvExplain != null) {
                                    htmlContent = result
                                    ExplainLinkUtil.setTextViewHTML(tvExplain, result)
                                }
                                it.setText(R.string.go_to_explainxkcd)
                                hasExplainedMore = true
                                isLoading = false
                            }

                            override fun explanationFailed() {
                                if (dialog.isShowing) {
                                    Toast.makeText(v.context, R.string.toast_more_explain_failed, Toast.LENGTH_SHORT).show()
                                    pbLoading.visibility = View.GONE
                                    it.setText(R.string.more_on_explainxkcd)
                                    hasExplainedMore = true
                                    isLoading = false
                                }
                            }
                        })
                        pbLoading.visibility = View.VISIBLE
                        isLoading = true
                    } else {
                        mListener!!.onNegativeClick()
                        dismiss()
                    }
                }
            })
        }
    }

    fun setListener(listener: ISimpleInfoDialogListener) {
        mListener = listener
    }

    fun setPic(pic: XkcdPic) {
        this.xkcdContent = pic.alt
    }

    fun setExtraExplain(string: String?) {
        pbLoading.visibility = View.GONE
        if (!string.isNullOrBlank()) {
            ExplainLinkUtil.setTextViewHTML(tvExplain, string)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            xkcdContent = savedInstanceState.getString(CONTENT)
            htmlContent = savedInstanceState.getString(HTML_CONTENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CONTENT, xkcdContent)
        outState.putString(HTML_CONTENT, htmlContent)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_explain, null)
        pbLoading = view.findViewById(R.id.pbLoading)
        tvExplain = view.findViewById(R.id.tvExplain)
        var negativeBtnTextId = R.string.dialog_more_details
        if (TextUtils.isEmpty(htmlContent) && TextUtils.isEmpty(xkcdContent)) {
            // For extra comics
            pbLoading.visibility = View.VISIBLE
            negativeBtnTextId = R.string.go_to_explainxkcd
            hasExplainedMore = true
        } else if (TextUtils.isEmpty(htmlContent)) {
            tvExplain.text = xkcdContent
        } else {
            tvExplain.text = HtmlCompat.fromHtml(htmlContent!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvExplain.movementMethod = CustomMovementMethod.getInstance()
            negativeBtnTextId = R.string.go_to_explainxkcd
            hasExplainedMore = true
        }
        return AlertDialog.Builder(activity!!).setView(view)
                .setPositiveButton(R.string.dialog_got_it) { _, _ ->
                    mListener!!.onPositiveClick()
                    dismiss()
                }
                .setNegativeButton(negativeBtnTextId, null)
                .create()
                .also { it.setOnShowListener(showListener) }
    }

    override fun onDestroyView() {
        mListener = null
        super.onDestroyView()
    }

    interface ISimpleInfoDialogListener {
        fun onPositiveClick()

        fun onNegativeClick()

        fun onExplainMoreClick(explainingCallback: ExplainingCallback)
    }

    interface ExplainingCallback {
        fun explanationLoaded(result: String)

        fun explanationFailed()
    }

    companion object {

        private const val CONTENT = "content"

        private const val HTML_CONTENT = "html_content"
    }
}
