package xyz.jienan.xkcd.ui.like

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

private val ANIMATING = "animating".hashCode()

fun LikeButton.animateShow(destTranslateX: Float) {
    animate(true, destTranslateX)
}

fun LikeButton.animateHide(destTranslateX: Float) {
    animate(false, destTranslateX)
}

private fun LikeButton.animate(isShow: Boolean, destTranslateX: Float) {
    (getTag(ANIMATING) as AnimatorSet?)?.cancel()

    AnimatorSet().apply {
        playTogether(ObjectAnimator.ofFloat<View>(this@animate, View.TRANSLATION_X, translationX, destTranslateX),
                ObjectAnimator.ofFloat<View>(this@animate, View.ALPHA, alpha, if (isShow) 1f else 0f))
        duration = 300L
        addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animator: Animator) {
                if (this@animate != null) {
                    isClickable = isShow
                    setTag(ANIMATING, this@apply)
                    if (isShow) {
                        this@animate.visibility = View.VISIBLE
                    }
                }
            }

            override fun onAnimationEnd(animator: Animator) {
                if (this@animate != null) {
                    setTag(ANIMATING, null)
                    if (!isShow) {
                        this@animate.visibility = View.GONE
                    }
                }
            }
        })
    }.start()
}