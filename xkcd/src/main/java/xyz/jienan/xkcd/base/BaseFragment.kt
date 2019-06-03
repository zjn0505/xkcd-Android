package xyz.jienan.xkcd.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import xyz.jienan.xkcd.Const.FIRE_UX_ACTION

abstract class BaseFragment : Fragment() {

    private val mFirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context!!) }

    @get:LayoutRes
    protected abstract val layoutResId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    @JvmOverloads
    protected fun logUXEvent(event: String, params: Map<String, String>? = null) {
        val bundle = Bundle()
        bundle.putString(FIRE_UX_ACTION, event)
        if (!params.isNullOrEmpty()) {
            for (key in params.keys) {
                val value = params[key]
                if (value!!.matches("-?\\d+".toRegex())) {
                    bundle.putInt(key, Integer.valueOf(value))
                } else {
                    bundle.putString(key, params[key])
                }
            }
        }
        mFirebaseAnalytics.logEvent(FIRE_UX_ACTION, bundle)
    }
}
