package xyz.jienan.xkcd.ui

import android.view.animation.Animation

interface Progressable {
    var progress: Int
}

interface Animatable {
    fun getAnimation() : Animation?

    fun startAnimation(animation: Animation)

    fun clearAnimation()

    fun setVisibility(visibility: Int)
}

interface IProgressbar : Progressable, Animatable