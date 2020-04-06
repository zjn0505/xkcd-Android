package xyz.jienan.xkcd.comics.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
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
import xyz.jienan.xkcd.base.glide.fallback
import xyz.jienan.xkcd.base.network.XKCD_BASE_URL
import xyz.jienan.xkcd.base.network.XKCD_EXPLAIN_URL
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity
import xyz.jienan.xkcd.comics.activity.ImageWebViewActivity
import xyz.jienan.xkcd.comics.activity.ImageWebViewActivity.Companion.TAG_XK3D
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

    private val singleComicPresenter: SingleComicContract.Presenter by lazy { SingleComicPresenter(this, PreferenceManager.getDefaultSharedPreferences(context)) }

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
            singleComicPresenter.getExplain(currentPic!!.num)
            logUXEvent(FIRE_MORE_EXPLAIN)
        }
    }

    private class GlideListener internal constructor(singleComicFragment: SingleComicFragment) : RequestListener<String, Bitmap> {

        private val weakReference: WeakReference<SingleComicFragment> = WeakReference(singleComicFragment)

        override fun onException(e: Exception?, model: String,
                                 target: Target<Bitmap>, isFirstResource: Boolean): Boolean {

            val fragment = weakReference.get() ?: return false

            if (fragment.btnReload == null) {
                return false
            }

            val fallback = model.fallback()
            if (fallback != model) {
                fragment.load(fallback)
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
            fragment.singleComicPresenter.updateXkcdSize(fragment.currentPic, resource)
            return false
        }
    }

    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override var translationMode = -1 // -1 unavailable, 0 off, 1 on
        set(value) {
            when (value) {
                -1 -> btnSeeTranslation?.visibility = View.GONE
                0 -> {
                    btnSeeTranslation?.visibility = View.VISIBLE
                    btnSeeTranslation?.text = resources.getText(R.string.see_translation)
                    btnSeeTranslation?.setOnClickListener {
                        translationMode = 1
                        singleComicPresenter.loadXkcd(ind)
                    }
                }
                1 -> {
                    btnSeeTranslation?.visibility = View.VISIBLE
                    btnSeeTranslation?.text = resources.getText(R.string.see_original)
                    btnSeeTranslation?.setOnClickListener {
                        translationMode = 0
                        singleComicPresenter.loadXkcd(ind)
                    }
                }
            }
            field = value
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
        val args = arguments
        if (args != null) {
            ind = args.getInt("ind")
        }
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pbLoading?.clearAnimation()
        pbLoading?.animation = AnimationUtils.loadAnimation(pbLoading!!.context, R.anim.rotate)
        initGlide()

        if (savedInstanceState != null) {
            dialogFragment = childFragmentManager
                    .findFragmentByTag("AltInfoDialogFragment") as SimpleInfoDialogFragment?
            dialogFragment?.setListener(dialogListener)

            if (singleComicPresenter.showLocalXkcd) {
                translationMode = savedInstanceState.getInt(KEY_TRANS_MODE, -1)
            }
        }

        singleComicPresenter.loadXkcd(ind)

        ivXkcdPic.setOnLongClickListener {
            if (currentPic == null) {
                false
            } else {
                showInfoDialog()
                it.performHapticFeedback(LONG_PRESS)
                true
            }
        }
    }

    override fun onDestroyView() {
        singleComicPresenter.onDestroy()
        dialogFragment = null
        Glide.clear(target!!)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (dialogFragment != null && dialogFragment!!.isAdded) {
            dialogFragment!!.dismissAllowingStateLoss()
            dialogFragment = null
        }
        if (translationMode != -1) {
            outState.putInt(KEY_TRANS_MODE, translationMode)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (ind <= 880) menu.findItem(R.id.action_go_xk3d).isVisible = true
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (currentPic == null) {
            return false
        }
        if ((parentFragment as ComicsMainFragment).currentIndex != currentPic!!.num.toInt()) {
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
            R.id.action_go_xk3d -> {
                ImageWebViewActivity.startActivity(activity!!, currentPic!!.num, translationMode == 1, webPageMode = TAG_XK3D)
                logUXEvent(FIRE_GO_XK3D_MENU)
                return true
            }
            R.id.action_go_explain -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(XKCD_EXPLAIN_URL + currentPic!!.num))
                startActivity(browserIntent)
                logUXEvent(FIRE_GO_EXPLAIN_MENU)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
        val interactiveComics = resources.getIntArray(R.array.interactive_comics)
        val prefInteractive = sharedPref.getBoolean(PREF_XKCD_INTERACTIVE, true)

        if (interactiveComics.contains(currentPic!!.num.toInt()) && prefInteractive) {
            ImageWebViewActivity.startActivity(activity!!, currentPic!!.num, translationMode == 1)
        } else {
            ImageDetailPageActivity.startActivity(activity!!, currentPic!!.targetImg, currentPic!!.num)
            activity!!.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }
    }

    private fun showInfoDialog() {
        dialogFragment = SimpleInfoDialogFragment()
        dialogFragment!!.setPic(currentPic!!)
        dialogFragment!!.setListener(dialogListener)
        dialogFragment!!.show(childFragmentManager, "AltInfoDialogFragment")
        logUXEvent(FIRE_LONG_PRESS)
    }

    override fun renderXkcdPic(xkcdPic: XkcdPic) {
        if (activity == null || activity!!.isFinishing) {
            return
        }

        if (target != null) {
            Glide.clear(target)
        }

        if (target != null && target!!.model.isNullOrBlank()) {
            target!!.model = xkcdPic.targetImg
            load(xkcdPic.targetImg)
        }

        currentPic = xkcdPic
        Timber.i("Pic to be loaded: $ind - ${xkcdPic.targetImg}")
        @SuppressLint("SetTextI18n")
        tvTitle!!.text = "${xkcdPic.num}. ${xkcdPic.title}"
        tvCreateDate?.text = String.format(getString(R.string.created_on), xkcdPic.year, xkcdPic.month, xkcdPic.day)
        tvDescription?.text = xkcdPic.alt
        tvCreateDate?.setOnClickListener {
            if (parentFragment is ComicsMainFragment) {
                (parentFragment as ComicsMainFragment).hearShake()
            }
        }
    }

    private fun load(url: String) {
        if (target != null) {
            Glide.clear(target)
        }
        Glide.with(activity)
                .load(url)
                .asBitmap()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(GlideListener(this))
                .into(target!!)
    }

    companion object {

        private const val KEY_TRANS_MODE = "transMode"

        fun newInstance(comicId: Int) =
                SingleComicFragment().apply { arguments = bundleOf("ind" to comicId) }
    }
}
