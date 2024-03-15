package com.sd.lib.network

import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 如果网络可用，直接返回；如果网络不可用，会挂起直到网络可用。
 */
suspend fun fAwaitNetworkAvailable() {
    if (fIsNetworkAvailable) return
    suspendCancellableCoroutine { continuation ->
        if (fIsNetworkAvailable) {
            continuation.resumeWith(Result.success(Unit))
        } else {
            object : FNetworkObserver() {
                override fun onChange(isAvailable: Boolean) {
                    if (isAvailable) {
                        unregister()
                        continuation.resumeWith(Result.success(Unit))
                    }
                }
            }.also { observer ->
                observer.register()
                continuation.invokeOnCancellation { observer.unregister() }
            }
        }
    }
}