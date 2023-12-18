package com.sd.lib.network

import com.sd.lib.ctx.fContext
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class FNetworkObserver {

    private val _observer = networkObserver(
        onAvailable = { this@FNetworkObserver.onAvailable() },
        onLost = { this@FNetworkObserver.onLost() },
    )

    /**
     * 注册
     */
    fun register() {
        _observer.register()
        _observer.notifyCallback()
    }

    /**
     * 取消注册
     */
    fun unregister() {
        _observer.unregister()
    }

    /**
     * 网络可用（主线程）
     */
    abstract fun onAvailable()

    /**
     * 网络不可用（主线程）
     */
    abstract fun onLost()

    companion object {
        @JvmStatic
        fun isNetworkAvailable(): Boolean {
            return libIsNetworkAvailable(fContext)
        }
    }
}

/**
 * 网络是否可用
 */
val fIsNetworkAvailable: Boolean get() = FNetworkObserver.isNetworkAvailable()

/**
 * 如果网络可用，直接返回；如果网络不可用，会挂起直到网络可用。
 */
suspend fun fAwaitNetworkAvailable() {
    if (fIsNetworkAvailable) return
    return suspendCancellableCoroutine { cont ->
        val observer = object : FNetworkObserver() {
            override fun onAvailable() {
                unregister()
                cont.resumeWith(Result.success(Unit))
            }

            override fun onLost() {}
        }

        cont.invokeOnCancellation {
            observer.unregister()
        }

        observer.register()
    }
}