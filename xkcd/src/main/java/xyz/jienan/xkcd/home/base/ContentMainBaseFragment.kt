package xyz.jienan.xkcd.home.base

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.app.SearchManager
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.squareup.seismic.ShakeDetector
import kotlinx.android.synthetic.main.fab_sub_icons.*
import kotlinx.android.synthetic.main.fragment_comic_main.*
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseFragment
import xyz.jienan.xkcd.comics.SearchCursorAdapter
import xyz.jienan.xkcd.comics.dialog.NumberPickerDialogFragment
import xyz.jienan.xkcd.home.MainActivity
import xyz.jienan.xkcd.model.WhatIfArticle
import xyz.jienan.xkcd.model.XkcdPic
import xyz.jienan.xkcd.model.persist.BoxManager
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.ui.NotificationUtils
import xyz.jienan.xkcd.ui.ToastUtils
import xyz.jienan.xkcd.ui.like.LikeButton
import xyz.jienan.xkcd.ui.like.OnLikeListener
import xyz.jienan.xkcd.ui.like.animateHide
import xyz.jienan.xkcd.ui.like.animateShow
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs

abstract class ContentMainBaseFragment : BaseFragment(), ShakeDetector.Listener {

    val isFabShowing: Boolean
        get() = fab.isShown

    protected abstract val adapter: BaseStatePagerAdapter

    protected abstract val presenter: ContentMainBasePresenter

    protected open var searchItemBackgroundRes: Int? = null

    protected val searchAdapter by lazy { SearchCursorAdapter(context, itemBgColor = searchItemBackgroundRes) }

    protected var latestIndex = INVALID_ID

    private var isFre = false

    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    private var isFabsShowing = false

    private val sd by lazy { ShakeDetector(this) }

    private var lastViewedId = INVALID_ID

    private val subFabAnimationDistance by lazy { fab!!.width.toFloat() }

    private val likeListener = object : OnLikeListener {
        override fun liked(likeButton: LikeButton) {
            when (likeButton.id) {
                R.id.btnFav -> {
                    presenter.favorited(currentIndex.toLong(), true)
                    logSubUXEvent(FIRE_FAVORITE_ON)
                }
                R.id.btnThumb -> {
                    presenter.liked(currentIndex.toLong())
                    logSubUXEvent(FIRE_THUMB_UP)
                }
            }
        }

        override fun unliked(likeButton: LikeButton) {
            when (likeButton.id) {
                R.id.btnFav -> {
                    presenter.favorited(currentIndex.toLong(), false)
                    logSubUXEvent(FIRE_FAVORITE_OFF)
                }
            }
        }
    }

    private val pickerListener = object : NumberPickerDialogFragment.INumberPickerDialogListener {
        override fun onPositiveClick(number: Int) {
            scrollViewPagerToItem(number - 1, false)
        }

        override fun onNegativeClick() {
            // Do nothing
        }
    }

    private val titleGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onTabTitleDoubleTap()
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            onTabTitleLongPress()
        }

        override fun onDown(e: MotionEvent?) = true
    }

    private val titleGestureDetector = GestureDetector(activity, titleGestureListener)

    protected abstract val titleTextRes: String

    protected abstract val pickerTitleTextRes: Int

    val currentIndex: Int
        get() = viewPager!!.currentItem + 1

    protected abstract val searchHint: CharSequence

    protected abstract fun suggestionClicked(position: Int)

    protected abstract fun updateFab()

    protected abstract fun onTabTitleLongPress()

    protected abstract fun onTabTitleDoubleTap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnFav.setOnLikeListener(likeListener)
        btnThumb.setOnLikeListener(likeListener)
        viewPager.adapter = adapter

        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = titleTextRes
        actionBar?.subtitle = "1     "

        val toolbar = (activity as AppCompatActivity).window.decorView.findViewById<ViewGroup>(R.id.toolbar)

        toolbar.forEach {
            if (it is TextView) {
                it.setOnTouchListener { _, motionEvent -> titleGestureDetector.onTouchEvent(motionEvent) }
            }
        }

        viewPager.offscreenPageLimit = 1

        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_DRAGGING) {
                    fab?.hide()
                    toggleSubFabs(false)
                } else if (state == SCROLL_STATE_IDLE) {
                    updateFab()
                }
            }

            override fun onPageSelected(position: Int) {
                (activity as MainActivity).toggleDrawerAvailability(true)
                actionBar?.subtitle = "%-5d".format(position + 1)
            }
        })
        presenter.loadLatest()
        latestIndex = presenter.latest
        lastViewedId = presenter.getLastViewed(latestIndex)
        if (savedInstanceState != null) {
            actionBar?.subtitle = "%-5d".format(lastViewedId)
            val pickerDialog = childFragmentManager.findFragmentByTag(NumberPickerDialogFragment.TAG) as NumberPickerDialogFragment?
            pickerDialog?.setListener(pickerListener)
        } else if (activity?.intent != null) {
            val notiIndex = requireActivity().intent.getIntExtra(INDEX_ON_NOTI_INTENT, INVALID_ID)

            if (notiIndex != INVALID_ID) {
                lastViewedId = notiIndex
                latestIndex = lastViewedId
                presenter.latest = latestIndex
                logSubUXEvent(FIRE_FROM_NOTIFICATION, mapOf(FIRE_FROM_NOTIFICATION_INDEX to notiIndex.toString()))
            }
            requireActivity().intent = null
        }
        isFre = latestIndex == INVALID_ID
        if (latestIndex > INVALID_ID) {
            adapter.size = latestIndex
            scrollViewPagerToItem(if (lastViewedId > INVALID_ID) lastViewedId - 1 else latestIndex - 1, false)
        }

        sd.start(activity?.getSystemService(SENSOR_SERVICE) as SensorManager, SensorManager.SENSOR_DELAY_GAME)

        fab.setOnClickListener { toggleSubFabs(!isFabsShowing) }
    }

    override fun onDestroyView() {
        sd.stop()
        presenter.onDestroy()
        super.onDestroyView()
    }

    override fun onStop() {
        if (viewPager != null && latestIndex > INVALID_ID) {
            val lastViewed = viewPager!!.currentItem + 1
            presenter.setLastViewed(lastViewed)
        }
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_LIST_ACTIVITY && resultCode == RESULT_OK && data != null) {
            val targetId = data.getIntExtra(INTENT_TARGET_XKCD_ID, INVALID_ID)
            if (targetId != INVALID_ID) {
                scrollViewPagerToItem(targetId - 1, false)
            }
        }
    }

    fun toggleFab(isFavorite: Boolean) {
        if (isFavorite) {
            fabAnimation(R.color.pink, R.color.white, R.drawable.ic_heart_on)
        } else {
            fabAnimation(R.color.white, R.color.pink, R.drawable.ic_heart_white)
        }
    }

    private fun fabAnimation(@ColorRes startColor: Int, @ColorRes endColor: Int, @DrawableRes icon: Int) {
        val animator = ObjectAnimator.ofInt(fab, "backgroundTint",
                ContextCompat.getColor(requireContext(), startColor), ContextCompat.getColor(requireContext(), endColor))

        animator.apply {
            duration = 1800L
            setEvaluator(ArgbEvaluator())
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener {
                fab?.backgroundTintList = ColorStateList.valueOf(animatedValue as Int)
            }
        }.start()
        fab.setImageResource(icon)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        menu.findItem(R.id.action_right).actionView = ImageButton(context).apply {
            setImageResource(R.drawable.ic_action_right)
            background = null
            setOnLongClickListener(MenuOnLongClickListener(this@ContentMainBaseFragment, false))
            setOnClickListener(MenuClickListener(this@ContentMainBaseFragment, false))
        }

        menu.findItem(R.id.action_left).actionView = ImageButton(context).apply {
            setImageResource(R.drawable.ic_action_left)
            background = null
            setOnLongClickListener(MenuOnLongClickListener(this@ContentMainBaseFragment, true))
            setOnClickListener(MenuClickListener(this@ContentMainBaseFragment, true))
        }

        setupSearch(menu)
    }

    private class MenuClickListener internal constructor(fragment: ContentMainBaseFragment,
                                                         private val isPrevious: Boolean) : View.OnClickListener {

        private val weakReference: WeakReference<ContentMainBaseFragment> = WeakReference(fragment)

        override fun onClick(v: View) {
            val fragment = weakReference.get() ?: return
            val skipCount = fragment.getString(
                    fragment.resources.getIdentifier(
                            fragment.sharedPreferences?.getString(PREF_ARROW, "arrow_1"),
                            "string",
                            fragment.activity?.packageName))
            var skip = Integer.parseInt(skipCount)
            skip = if (isPrevious) -skip else skip

            val current = fragment.viewPager?.currentItem ?: 0

            val smoothScroll = if (abs(skip) != 1) {
                false
            } else if (isPrevious && current != 0) {
                true
            } else !isPrevious && current != fragment.latestIndex.minus(1)

            fragment.scrollViewPagerToItem(current + skip, smoothScroll)

            fragment.logSubUXEvent(if (isPrevious) FIRE_PREVIOUS_BAR else FIRE_NEXT_BAR)
        }
    }

    private class MenuOnLongClickListener internal constructor(fragment: ContentMainBaseFragment,
                                                               private val isPrevious: Boolean) : View.OnLongClickListener {

        private val weakReference: WeakReference<ContentMainBaseFragment> = WeakReference(fragment)

        override fun onLongClick(view: View): Boolean {
            val fragment = weakReference.get() ?: return false
            val current = fragment.viewPager?.currentItem ?: 0

            val smoothScroll = if (isPrevious && current != 0) {
                true
            } else !isPrevious && current != fragment.latestIndex.minus(1)

            fragment.scrollViewPagerToItem(if (isPrevious) 0 else fragment.latestIndex - 1, smoothScroll)
            fragment.logSubUXEvent(if (isPrevious) FIRE_PREVIOUS_BAR_LONG else FIRE_NEXT_BAR_LONG)
            view.performHapticFeedback(LONG_PRESS)
            return true
        }
    }

    override fun hearShake() {
        if (!isResumed) {
            return
        }

        val prefRandom = sharedPreferences!!.getString(PREF_RANDOM, "random_all")

        if ("random_disabled" == prefRandom) {
            return
        }

        latestIndex = presenter.latest
        if (latestIndex != INVALID_ID) {
            val randomId = if ("random_all" == prefRandom) {
                Random().nextInt(latestIndex + 1)
            } else {
                presenter.randomUntouchedIndex.toInt()
            }
            scrollViewPagerToItem(randomId - 1, false)
        }
        view?.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING)
        logSubUXEvent(FIRE_SHAKE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> logSubUXEvent(FIRE_SEARCH)
            R.id.action_specific, R.id.action_what_if_specific -> {
                if (latestIndex == INVALID_ID) {
                    return false
                }
                val pickerDialogFragment = childFragmentManager.findFragmentByTag(NumberPickerDialogFragment.TAG)
                        as NumberPickerDialogFragment? ?: NumberPickerDialogFragment()
                pickerDialogFragment.apply {
                    setNumberRange(1, latestIndex)
                    setListener(pickerListener)
                    setTitle(pickerTitleTextRes)
                }.show(childFragmentManager, NumberPickerDialogFragment.TAG)

                logSubUXEvent(FIRE_SPECIFIC_MENU)
            }
            R.id.test_notification -> if (titleTextRes == TAG_XKCD) {
                val xkcd = BoxManager.getXkcd(latestIndex.toLong())
                BoxManager.xkcdBox.remove(latestIndex.toLong())
                latestIndex -= 1
                presenter.latest = latestIndex
                presenter.setLastViewed(10)
                adapter.size = latestIndex
                xkcdNoti(xkcd!!)
            } else if (titleTextRes == TAG_WHAT_IF) {
                val whatIfArticle = BoxManager.getWhatIf(latestIndex.toLong())
                BoxManager.whatIfBox.remove(latestIndex.toLong())
                latestIndex -= 1
                presenter.latest = latestIndex
                presenter.setLastViewed(10)
                adapter.size = latestIndex
                whatIfNoti(whatIfArticle!!)
            }
        }
        return false
    }

    private fun xkcdNoti(xkcdPic: XkcdPic) {
        if (latestIndex >= xkcdPic.num) {
            return  // User already read the latest comic
        } else {
            SharedPrefManager.latestXkcd = latestIndex.toLong()
            BoxManager.updateAndSave(xkcdPic)
        }
        if (activity != null) {
            NotificationUtils.showNotification(requireContext().applicationContext, xkcdPic)
        }
    }

    private fun whatIfNoti(whatIfArticle: WhatIfArticle) {
        if (latestIndex >= whatIfArticle.num) {
            return  // User already read the what ifF
        } else {
            SharedPrefManager.latestWhatIf = latestIndex.toLong()
            BoxManager.updateAndSaveWhatIf(mutableListOf(whatIfArticle))
        }
        if (activity != null) {
            NotificationUtils.showNotification(requireContext().applicationContext, whatIfArticle)
        }
    }

    fun scrollViewPagerToItem(id: Int, smoothScroll: Boolean) {
        viewPager.setCurrentItem(id, smoothScroll)
        fab?.hide()
        toggleSubFabs(false)
        if (!smoothScroll) {
            presenter.getInfoAndShowFab(currentIndex)
        }
    }

    fun expand(size: Int) {
        if (size > adapter.size) {
            adapter.size = size
        }
    }

    protected fun toggleSubFabs(showSubFabs: Boolean) {
        if (fab?.width == 0) {
            return
        }

        val distance = -subFabAnimationDistance * 1.3f
        if (showSubFabs) {
            btnFav?.animateShow(distance * 2)
            btnThumb?.animateShow(distance)
        } else {
            btnFav?.animateHide(distance)
            btnThumb?.animateHide(0f)
        }

        isFabsShowing = showSubFabs
    }

    protected fun showToast(context: Context, text: String) {
        ToastUtils.showToast(context, text)
    }

    protected fun latestLoaded() {
        adapter.size = latestIndex
        if (isFre) {
            scrollViewPagerToItem(latestIndex - 1, false)
        }
        presenter.latest = latestIndex
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        (searchItem.actionView as? SearchView)?.apply {
            queryHint = searchHint
            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)?.threshold = 1
            suggestionsAdapter = searchAdapter
            setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    suggestionClicked(position)
                    clearFocus()
                    searchItem.collapseActionView()
                    return true
                }
            })
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isBlank()) {
                        return false
                    }
                    presenter.searchContent(newText.trim())
                    return true
                }
            })
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                if (activity != null) {
                    (activity as MainActivity).toggleDrawerAvailability(false)
                    setItemsVisibility(menu, intArrayOf(R.id.action_left, R.id.action_right, R.id.action_xkcd_list, R.id.action_what_if_list), false)
                }
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                if (activity != null) {
                    (activity as MainActivity).toggleDrawerAvailability(true)
                    setItemsVisibility(menu, intArrayOf(R.id.action_left, R.id.action_right, R.id.action_xkcd_list, R.id.action_what_if_list), true)
                }
                return true
            }
        })
    }

    private fun setItemsVisibility(menu: Menu, hideItems: IntArray, visible: Boolean) {
        for (hideItem in hideItems) {
            menu.findItem(hideItem)?.isVisible = visible
        }
    }

    private fun logSubUXEvent(event: String, params: Map<String, String>? = null) {
        val suffix = if (titleTextRes == TAG_XKCD) "" else FIRE_WHAT_IF_SUFFIX
        logUXEvent(event + suffix, params)
    }

    companion object {

        const val REQ_LIST_ACTIVITY = 10
    }
}
