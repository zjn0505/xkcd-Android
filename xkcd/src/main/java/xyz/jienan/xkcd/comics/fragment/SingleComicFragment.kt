package xyz.jienan.xkcd.comics.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_comic_single.*
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseFragment
import xyz.jienan.xkcd.base.glide.MyProgressTarget
import xyz.jienan.xkcd.base.glide.ProgressTarget
import xyz.jienan.xkcd.base.network.XKCD_BASE_URL
import xyz.jienan.xkcd.base.network.XKCD_EXPLAIN_URL
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity
import xyz.jienan.xkcd.comics.contract.SingleComicContract
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment.ISimpleInfoDialogListener
import xyz.jienan.xkcd.comics.presenter.SingleComicPresenter
import xyz.jienan.xkcd.model.XkcdPic
import java.lang.ref.WeakReference

/**
 * Created by jienanzhang on 03/03/2018.
 */

class SingleComicFragment : BaseFragment(), SingleComicContract.View {

    override val layoutResId = R.layout.fragment_comic_single

    private var dialogFragment: SimpleInfoDialogFragment? = null

    private var ind: Int = 0

    private var currentPic: XkcdPic? = null

    private var target: ProgressTarget<String, Bitmap>? = null

    private var explainingCallback: SimpleInfoDialogFragment.ExplainingCallback? = null

    private var singleComicPresenter: SingleComicContract.Presenter? = null

    private val dialogListener = object : ISimpleInfoDialogListener {
        override fun onPositiveClick() {
            // Do nothing
        }

        override fun onNegativeClick() {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_EXPLAIN_URL + currentPic!!.num))
            if (browserIntent.resolveActivity(activity!!.packageManager) != null) {
                startActivity(browserIntent)
            }
        }

        @SuppressLint("StaticFieldLeak", "CheckResult")
        override fun onExplainMoreClick(explainingCallback: SimpleInfoDialogFragment.ExplainingCallback) {
            this@SingleComicFragment.explainingCallback = explainingCallback
            singleComicPresenter!!.getExplain(currentPic!!.num)
            logUXEvent(FIRE_MORE_EXPLAIN)
        }
    }

    private class GlideListener internal constructor(singleComicFragment: SingleComicFragment) : RequestListener<String, Bitmap> {

        private val weakReference: WeakReference<SingleComicFragment> = WeakReference(singleComicFragment)

        override fun onException(e: Exception, model: String,
                                 target: Target<Bitmap>, isFirstResource: Boolean): Boolean {

            val fragment = weakReference.get() ?: return false

            if (fragment.btnReload == null) {
                return false
            }

            if (model.startsWith("https")) {
                fragment.load(model.replaceFirst("https".toRegex(), "http"))
                return true
            }

            fragment.btnReload?.visibility = View.VISIBLE
            fragment.pbLoading?.visibility = View.GONE

            fragment.btnReload?.setOnClickListener {
                fragment.pbLoading?.visibility = View.VISIBLE
                fragment.pbLoading?.clearAnimation()
                fragment.pbLoading?.animation = AnimationUtils.loadAnimation(fragment.pbLoading?.context, R.anim.rotate)
                Glide.with(fragment.activity)
                        .load(fragment.currentPic?.img)
                        .asBitmap()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(this)
                        .into(target)
            }
            return false
        }

        override fun onResourceReady(resource: Bitmap, model: String,
                                     target: Target<Bitmap>, isFromMemoryCache: Boolean,
                                     isFirstResource: Boolean): Boolean {
            val fragment = weakReference.get() ?: return false

            fragment.btnReload?.visibility = View.GONE
            fragment.ivXkcdPic?.setOnClickListener { fragment.launchDetailPageActivity() }
            fragment.singleComicPresenter?.updateXkcdSize(fragment.currentPic, resource)
            return false
        }
    }

    override fun explainLoaded(result: String) {
        if (!TextUtils.isEmpty(result)) {
            explainingCallback!!.explanationLoaded(result)
        } else {
            explainingCallback!!.explanationFailed()
        }
    }

    override fun explainFailed() {
        explainingCallback!!.explanationFailed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singleComicPresenter = SingleComicPresenter(this)
        val args = arguments
        if (args != null) {
            ind = args.getInt("ind")
        }
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pbLoading!!.clearAnimation()
        pbLoading!!.animation = AnimationUtils.loadAnimation(pbLoading!!.context, R.anim.rotate)
        initGlide()
        singleComicPresenter!!.loadXkcd(ind)

        if (savedInstanceState != null) {
            dialogFragment = childFragmentManager
                    .findFragmentByTag("AltInfoDialogFragment") as SimpleInfoDialogFragment?
            dialogFragment?.setListener(dialogListener)
        }
        ivXkcdPic.setOnLongClickListener {
            if (currentPic == null) {
                false
            } else {
                showInfoDialog()
                it.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
                true
            }
        }
    }

    override fun onDestroyView() {
        singleComicPresenter?.onDestroy()
        dialogFragment = null
        Glide.clear(target!!)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (dialogFragment != null && dialogFragment!!.isAdded) {
            dialogFragment!!.dismissAllowingStateLoss()
            dialogFragment = null
        }
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (currentPic == null) {
            return false
        }
        when (item.itemId) {
            R.id.action_share -> {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, currentPic!!.targetImg))
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_to)))
                logUXEvent(FIRE_SHARE_BAR)
                return true
            }
            R.id.action_go_xkcd -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_BASE_URL + currentPic!!.num))
                startActivity(browserIntent)
                logUXEvent(FIRE_GO_XKCD_MENU)
                return true
            }
            R.id.action_go_explain -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_EXPLAIN_URL + currentPic!!.num))
                startActivity(browserIntent)
                logUXEvent(FIRE_GO_EXPLAIN_MENU)
                return true
            }
        }
        return false
    }

    override fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            pbLoading!!.visibility = View.VISIBLE
        } else {
            pbLoading!!.visibility = View.GONE
        }
    }

    private fun initGlide() {
        target = MyProgressTarget(BitmapImageViewTarget(ivXkcdPic!!), pbLoading, ivXkcdPic)
    }

    private fun launchDetailPageActivity() {
        if (currentPic == null || TextUtils.isEmpty(currentPic!!.targetImg)) {
            return
        }
        ImageDetailPageActivity.startActivity(activity!!, currentPic!!.targetImg, currentPic!!.num)
        activity!!.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    private fun showInfoDialog() {
        dialogFragment = SimpleInfoDialogFragment()
        dialogFragment!!.setPic(currentPic!!)
        dialogFragment!!.setListener(dialogListener)
        dialogFragment!!.show(childFragmentManager, "AltInfoDialogFragment")
        logUXEvent(FIRE_LONG_PRESS)
    }

    override fun renderXkcdPic(xPic: XkcdPic) {
        if (activity == null || activity!!.isFinishing) {
            return
        }
        if (target!!.model.isNullOrBlank()) {
            target!!.model = xPic.targetImg
            load(xPic.targetImg)
        }

        currentPic = xPic
        Timber.i("Pic to be loaded: $ind - ${xPic.targetImg}")
        @SuppressLint("SetTextI18n")
        tvTitle!!.text = "${xPic.num}. ${xPic.title}"
        tvCreateDate!!.text = String.format(getString(R.string.created_on), xPic.year, xPic.month, xPic.day)
        tvDescription?.text = xPic.alt
    }

    private fun load(url: String) {
        Glide.with(activity)
                .load(url)
                .asBitmap()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(GlideListener(this))
                .into(target!!)
    }

    companion object {
        fun newInstance(comicId: Int) =
                SingleComicFragment().apply { arguments = Bundle(1).apply { putInt("ind", comicId) } }
    }
}
