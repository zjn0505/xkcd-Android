package xyz.jienan.xkcd.ui.like

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import xyz.jienan.xkcd.R

@SuppressLint("AnimatorKeep")
class LikeButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val icon by lazy { findViewById<ImageView>(R.id.icon) }

    private val dotsView by lazy { findViewById<DotsView>(R.id.dots) }

    private val circleView by lazy { findViewById<CircleView>(R.id.circle) }

    private lateinit var currentIcon: Icon

    private var likeListener: OnLikeListener? = null

    private var iconSize = 0

    private var animationScaleFactor = 0f

    /**
     * Returns current like state
     *
     * @return current like state
     */
    var isLiked: Boolean = false
        set(value) =
            if (value) {
                field = true
                icon!!.setImageDrawable(likeDrawable)
            } else {
                field = false
                icon!!.setImageDrawable(unLikeDrawable)
            }

    private var isPersisted = false

    private val animatorSet by lazy {
        AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(circleView, "outerCircleRadiusProgress", 0.1f, 1f)
                            .apply { duration = 250; interpolator = DECELERATE_INTERPOLATOR },
                    ObjectAnimator.ofFloat(circleView, "innerCircleRadiusProgress", 0.1f, 1f)
                            .apply { duration = 200; startDelay = 200; interpolator = DECELERATE_INTERPOLATOR },
                    ObjectAnimator.ofFloat(icon, ImageView.SCALE_X, 0.2f, 1f)
                            .apply { duration = 350; startDelay = 250; interpolator = OVERSHOOT_INTERPOLATOR },
                    ObjectAnimator.ofFloat(icon, ImageView.SCALE_Y, 0.2f, 1f)
                            .apply { duration = 350; startDelay = 250; interpolator = OVERSHOOT_INTERPOLATOR },
                    ObjectAnimator.ofFloat(dotsView, "currentProgress", 0f, 1f)
                            .apply { duration = 900; startDelay = 50; interpolator = ACCELERATE_DECELERATE_INTERPOLATOR },
                    ObjectAnimator.ofFloat(icon, View.ALPHA, 0f, 1f)
                            .apply { duration = 800; startDelay = 150; interpolator = ACCELERATE_DECELERATE_INTERPOLATOR }
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    resetIcon(false)
                }
            })
        }
    }

    private var likeDrawable: Drawable? = null
    private var unLikeDrawable: Drawable? = null

    init {
        if (!isInEditMode)
            init(context, attrs, defStyleAttr)
    }

    /**
     * Does all the initial setup of the button such as retrieving all the attributes that were
     * set in xml and inflating the like button's view and initial state.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(getContext()).inflate(R.layout.likeview, this, true)

        val array = context.obtainStyledAttributes(attrs, R.styleable.LikeButton, defStyle, 0)

        iconSize = array.getDimensionPixelSize(R.styleable.LikeButton_icon_size, 40)

        val iconType = array.getString(R.styleable.LikeButton_icon_type)

        likeDrawable = getDrawableFromResource(array, R.styleable.LikeButton_like_drawable)

        if (likeDrawable != null)
            setLikeDrawable(likeDrawable!!)

        unLikeDrawable = getDrawableFromResource(array, R.styleable.LikeButton_unlike_drawable)

        if (unLikeDrawable != null)
            setUnlikeDrawable(unLikeDrawable!!)

        currentIcon = parseIconType(iconType ?: IconType.Heart.name)


        if (likeDrawable == null && unLikeDrawable == null) {
            if (currentIcon != null) {
                setIcon()
            } else {
                setIcon(IconType.Heart)
            }
        }

        isEnabled = array.getBoolean(R.styleable.LikeButton_is_enabled, true)
        val status = array.getBoolean(R.styleable.LikeButton_liked, false)
        isPersisted = array.getBoolean(R.styleable.LikeButton_persist_liked, false)
        setAnimationScaleFactor(array.getFloat(R.styleable.LikeButton_anim_scale_factor, 3f))
        isLiked = status
        setOnClickListener(this)
        array.recycle()
    }

    private fun getDrawableFromResource(array: TypedArray, styleableIndexId: Int): Drawable? {
        val id = array.getResourceId(styleableIndexId, -1)

        return if (-1 != id) ContextCompat.getDrawable(context, id) else null
    }

    /**
     * This triggers the entire functionality of the button such as icon changes,
     * animations, listeners etc.
     *
     * @param v
     */
    override fun onClick(v: View) {

        isLiked = !isLiked || isPersisted
        icon!!.setImageDrawable(if (isLiked) likeDrawable else unLikeDrawable)

        if (likeListener != null) {
            if (isLiked) {
                likeListener!!.liked(this)
            } else {
                likeListener!!.unliked(this)
            }
        }

        animatorSet.cancel()

        resetIcon(isLiked)

        if (isLiked) {
            animatorSet.start()
        }
    }

    private fun resetIcon(toMini: Boolean) {
        circleView!!.innerCircleRadiusProgress = 0f
        circleView!!.outerCircleRadiusProgress = 0f
        dotsView!!.currentProgress = 0f
        val scale = if (toMini) 0f else 1f
        icon!!.scaleX = scale
        icon!!.scaleY = scale
        icon!!.alpha = 1f
    }

    /**
     * Used to trigger the scale animation that takes places on the
     * icon when the button is touched.
     *
     * @param event
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                /*
                Commented out this line and moved the animation effect to the action up event due to
                conflicts that were occurring when library is used in sliding type views.
                icon.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).setInterpolator(DECELERATE_INTERPOLATOR);
                */
                isPressed = true

            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val isInside = x > 0 && x < width && y > 0 && y < height
                if (isPressed != isInside) {
                    isPressed = isInside
                }
            }

            MotionEvent.ACTION_UP -> {
                icon!!.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).interpolator = DECELERATE_INTERPOLATOR
                icon!!.animate().scaleX(1f).scaleY(1f).interpolator = DECELERATE_INTERPOLATOR
                if (isPressed) {
                    performClick()
                    isPressed = false
                }
            }
            MotionEvent.ACTION_CANCEL -> isPressed = false
        }
        return true
    }

    /**
     * This drawable is shown when the button is a liked state.
     *
     * @param resId
     */
    private fun setLikeDrawableRes(@DrawableRes resId: Int) {
        likeDrawable = ContextCompat.getDrawable(context, resId)

        if (iconSize != 0) {
            likeDrawable = LikeUtils.resizeDrawable(context, likeDrawable, iconSize, iconSize)
        }

        if (isLiked) {
            icon!!.setImageDrawable(likeDrawable)
        }
    }

    /**
     * This drawable is shown when the button is in a liked state.
     *
     * @param likeDrawable
     */
    private fun setLikeDrawable(likeDrawable: Drawable) {
        this.likeDrawable = likeDrawable

        if (iconSize != 0) {
            this.likeDrawable = LikeUtils.resizeDrawable(context, likeDrawable, iconSize, iconSize)
        }

        if (isLiked) {
            icon!!.setImageDrawable(this.likeDrawable)
        }
    }

    /**
     * This drawable will be shown when the button is in on unliked state.
     *
     * @param resId
     */
    private fun setUnlikeDrawableRes(@DrawableRes resId: Int) {
        unLikeDrawable = ContextCompat.getDrawable(context, resId)

        if (iconSize != 0) {
            unLikeDrawable = LikeUtils.resizeDrawable(context, unLikeDrawable, iconSize, iconSize)
        }

        if (!isLiked) {
            icon!!.setImageDrawable(unLikeDrawable)
        }
    }

    /**
     * This drawable will be shown when the button is in on unliked state.
     *
     * @param unLikeDrawable
     */
    private fun setUnlikeDrawable(unLikeDrawable: Drawable) {
        this.unLikeDrawable = unLikeDrawable

        if (iconSize != 0) {
            this.unLikeDrawable = LikeUtils.resizeDrawable(context, unLikeDrawable, iconSize, iconSize)
        }

        if (!isLiked) {
            icon!!.setImageDrawable(this.unLikeDrawable)
        }
    }

    /**
     * Sets one of the three icons that are bundled with the library.
     *
     * @param currentIconType
     */
    private fun setIcon(currentIconType: IconType) {
        currentIcon = parseIconType(currentIconType)
        setLikeDrawableRes(currentIcon.onIconResourceId)
        setUnlikeDrawableRes(currentIcon.offIconResourceId)
        icon!!.setImageDrawable(this.unLikeDrawable)
    }

    private fun setIcon() {
        setLikeDrawableRes(currentIcon.onIconResourceId)
        setUnlikeDrawableRes(currentIcon.offIconResourceId)
        icon!!.setImageDrawable(this.unLikeDrawable)
    }

    /**
     * * Parses the specific icon based on string
     * version of its enum.
     * These icons are bundled with the library and
     * are accessed via objects that contain their
     * resource ids and an enum with their name.
     *
     * @param iconType
     * @return Icon
     */
    private fun parseIconType(iconType: String): Icon {
        val icons = LikeUtils.getIcons()

        for (icon in icons) {
            if (icon.iconType.name.toLowerCase() == iconType.toLowerCase()) {
                return icon
            }
        }

        throw IllegalArgumentException("Correct icon type not specified.")
    }

    /**
     * Parses the specific icon based on it's type.
     * These icons are bundled with the library and
     * are accessed via objects that contain their
     * resource ids and an enum with their name.
     *
     * @param iconType
     * @return
     */
    private fun parseIconType(iconType: IconType): Icon {
        val icons = LikeUtils.getIcons()

        for (icon in icons) {
            if (icon.iconType == iconType) {
                return icon
            }
        }

        throw IllegalArgumentException("Correct icon type not specified.")
    }

    /**
     * Listener that is triggered once the
     * button is in a liked or unliked state
     *
     * @param likeListener
     */
    fun setOnLikeListener(likeListener: OnLikeListener) {
        this.likeListener = likeListener
    }

    /**
     * This function updates the dots view and the circle
     * view with the respective sizes based on the size
     * of the icon being used.
     */
    private fun setEffectsViewSize() {
        if (iconSize != 0) {
            dotsView!!.setSize((iconSize * animationScaleFactor).toInt(), (iconSize * animationScaleFactor).toInt())
            circleView!!.setSize(iconSize, iconSize)
        }
    }

    /**
     * Sets the factor by which the dots should be sized.
     */
    private fun setAnimationScaleFactor(animationScaleFactor: Float) {
        this.animationScaleFactor = animationScaleFactor

        setEffectsViewSize()
    }

    companion object {
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()
        private val ACCELERATE_DECELERATE_INTERPOLATOR = AccelerateDecelerateInterpolator()
        private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(4f)
    }
}