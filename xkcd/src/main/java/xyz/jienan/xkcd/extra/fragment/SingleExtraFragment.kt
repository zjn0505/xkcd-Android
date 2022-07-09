package xyz.jienan.xkcd.extra.fragment

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
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
import xyz.jienan.xkcd.comics.activity.ImageDetailPageActivity
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment
import xyz.jienan.xkcd.comics.dialog.SimpleInfoDialogFragment.ISimpleInfoDialogListener
import xyz.jienan.xkcd.extra.contract.SingleExtraContract
import xyz.jienan.xkcd.extra.presenter.SingleExtraPresenter
import xyz.jienan.xkcd.model.ExtraComics
import xyz.jienan.xkcd.model.ExtraModel
import xyz.jienan.xkcd.model.util.ExplainLinkUtil
import xyz.jienan.xkcd.model.util.XkcdSideloadUtils
import java.lang.ref.WeakReference

/**
 * Created by jienanzhang on 03/03/2018.
 */

class SingleExtraFragment : BaseFragment(), SingleExtraContract.View {
    override val layoutResId = R.layout.fragment_comic_single

    private var dialogFragment: SimpleInfoDialogFragment? = null

    private var ind: Int = 0

    private var currentExtra: ExtraComics? = null

    private var target: ProgressTarget<String, Bitmap>? = null

    private var singleExtraPresenter: SingleExtraContract.Presenter? = null

    private val dialogListener = object : ISimpleInfoDialogListener {
        override fun onPositiveClick() {
            // Do nothing
        }

        override fun onNegativeClick() {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentExtra!!.explainUrl))
            if (browserIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(browserIntent)
            }
        }

        override fun onExplainMoreClick(explainingCallback: SimpleInfoDialogFragment.ExplainingCallback) {
            // no-ops
        }
    }

    private class GlideListener internal constructor(singleExtraFragment: SingleExtraFragment) : RequestListener<String, Bitmap> {

        private val weakReference: WeakReference<SingleExtraFragment> = WeakReference(singleExtraFragment)

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

            fragment.btnReload!!.visibility = View.VISIBLE

            fragment.btnReload!!.setOnClickListener {
                fragment.pbLoading!!.clearAnimation()
                fragment.pbLoading!!.animation = AnimationUtils.loadAnimation(fragment.pbLoading!!.context, R.anim.rotate)
                Glide.with(fragment.activity).load(fragment.currentExtra!!.img).asBitmap().fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(this).into(target)

            }
            return false
        }

        override fun onResourceReady(resource: Bitmap, model: String,
                                     target: Target<Bitmap>, isFromMemoryCache: Boolean,
                                     isFirstResource: Boolean): Boolean {
            val fragment = weakReference.get() ?: return false

            fragment.btnReload!!.visibility = View.GONE
            if (fragment.ivXkcdPic != null) {
                fragment.ivXkcdPic!!.setOnClickListener { fragment.launchDetailPageActivity() }
            }
            return false
        }
    }

    override fun explainLoaded(result: String) {
        if (!TextUtils.isEmpty(result)) {
            val alt = if (currentExtra?.alt.isNullOrBlank()) {
                ""
            } else {
                currentExtra?.alt + "<br><br>"
            }
            if (dialogFragment != null && dialogFragment!!.isAdded) {
                dialogFragment!!.setExtraExplain(alt + result)
            }
            if (tvDescription != null) {
                ExplainLinkUtil.setTextViewHTML(tvDescription, alt + result)
            }
        } else {
            if (dialogFragment != null && dialogFragment!!.isAdded) {
                dialogFragment!!.setExtraExplain(null)
            }
        }
    }

    override fun explainFailed() {
        // no-ops
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singleExtraPresenter = SingleExtraPresenter(this)
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
        singleExtraPresenter!!.loadExtra(ind)

        if (savedInstanceState != null) {
            dialogFragment = childFragmentManager
                    .findFragmentByTag("AltInfoDialogFragment") as SimpleInfoDialogFragment?
            if (dialogFragment != null) {
                dialogFragment!!.setListener(dialogListener)
            }
        }
        ivXkcdPic.setOnLongClickListener {
            if (currentExtra == null) {
                false
            } else {
                dialogFragment = SimpleInfoDialogFragment()
                dialogFragment!!.setListener(dialogListener)
                dialogFragment!!.show(childFragmentManager, "AltInfoDialogFragment")
                it.performHapticFeedback(LONG_PRESS)
                singleExtraPresenter!!.getExplain(currentExtra!!.explainUrl, false)
                logUXEvent(FIRE_LONG_PRESS)
                true
            }
        }
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            scroll?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        singleExtraPresenter!!.onDestroy()
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
        if (currentExtra == null) {
            return false
        }
        if ((parentFragment as ExtraMainFragment).currentIndex != ind) {
            return false
        }
        when (item.itemId) {
            R.id.action_share -> {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, currentExtra!!.img))
                shareIntent.type = "text/plain"
                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_to)))
                logUXEvent(FIRE_SHARE_BAR + FIRE_EXTRA_SUFFIX)
                return true
            }
            R.id.action_go_explain -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentExtra!!.explainUrl))
                startActivity(browserIntent)
                logUXEvent(FIRE_GO_EXTRA_MENU)
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

    /**
     * Launch a new Activity to show the pic in full screen mode
     */
    private fun launchDetailPageActivity() {
        if (currentExtra == null || TextUtils.isEmpty(currentExtra!!.img)) {
            return
        }
        ImageDetailPageActivity.startActivity(requireActivity(), currentExtra!!.img, currentExtra!!.num)
        requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    /**
     * Render img, text on the view
     *
     * @param xPic
     */
    override fun renderExtraPic(xPic: ExtraComics?) {
        if (xPic == null || activity == null || requireActivity().isFinishing) {
            if (xPic == null) {
                XkcdSideloadUtils.loadExtra(requireContext())
            }
            return
        }
        if (TextUtils.isEmpty(target!!.model)) {
            target!!.model = xPic.img
            load(xPic.img)
        }

        currentExtra = xPic
        Timber.i("Pic to be loaded: %d - %s", ind, xPic.img)
        tvTitle!!.text = String.format("%d. %s", xPic.num, xPic.title)
        tvCreateDate!!.text = xPic.date
        singleExtraPresenter!!.getExplain(xPic.explainUrl, true)
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
                SingleExtraFragment().apply { arguments = bundleOf("ind" to comicId) }
    }
}
