package com.sd.lib.network

import kotlinx.coroutines.suspendCancellableCoroutine

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
            override fun onChange(isAvailable: Boolean) {
                if (isAvailable) {
                    unregister()
                    cont.resumeWith(Result.success(Unit))
                }
            }
        }

        cont.invokeOnCancellation {
            observer.unregister()
        }

        if (cont.isActive) {
            observer.register()
        }
    }
}