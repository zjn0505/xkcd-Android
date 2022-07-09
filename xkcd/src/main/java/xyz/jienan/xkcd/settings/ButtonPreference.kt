package xyz.jienan.xkcd.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import xyz.jienan.xkcd.R

class ButtonPreference : Preference {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    @StringRes
    private var stringRes: Int = 0

    private var onClickListener: ((View) -> Unit)? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val button = holder.findViewById(R.id.deleteButton)
        if (button != null && button is Button && onClickListener != null) {
            if (stringRes != 0) {
                button.setText(stringRes)
            }
            button.setOnClickListener(onClickListener)
        }
    }

    fun setup(summaryText: String? = null, @StringRes resId: Int = 0, onClickListener: (View) -> Unit) {
        summary = summaryText
        this.stringRes = resId
        this.onClickListener = onClickListener
    }
}