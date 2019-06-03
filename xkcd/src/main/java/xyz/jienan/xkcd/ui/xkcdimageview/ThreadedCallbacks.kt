package xyz.jienan.xkcd.ui.xkcdimageview

import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


/**
 * Created by Piasy{github.com/Piasy} on 08/04/2017.
 */
@Keep
class ThreadedCallbacks private constructor(private val mHandler: Handler, private val mTarget: Any?) : InvocationHandler {

    @Throws(Throwable::class)
    override operator fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
        if (method.returnType != Void.TYPE) {
            throw RuntimeException("Method should return void: $method")
        }
        if (Looper.myLooper() == mHandler.looper) {
            method.invoke(mTarget, *args)
        } else {
            mHandler.post {
                try {
                    method.invoke(mTarget, *args)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return NON_SENSE
    }

    companion object {

        private val NON_SENSE = Any()
        private val MAIN_HANDLER = Handler(Looper.getMainLooper())

        fun <T> create(type: Class<T>, target: T): T {
            return create(MAIN_HANDLER, type, target)
        }

        fun <T> create(handler: Handler, type: Class<T>, target: T): T {
            return Proxy.newProxyInstance(type.classLoader,
                    arrayOf<Class<*>>(type), ThreadedCallbacks(handler, target)) as T
        }
    }
}