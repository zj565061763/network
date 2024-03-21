package com.sd.lib.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 监听网络是否可用
 */
abstract class FNetworkAvailableObserver {
    private val _scope = MainScope()
    private var _job: Job? = null

    /**
     * 注册
     */
    @Synchronized
    fun register() {
        _job?.let { return }
        _job = _scope.launch {
            FNetwork.isAvailableFlow.collect {
                onChange(it)
            }
        }
    }

    /**
     * 取消注册
     */
    @Synchronized
    fun unregister() {
        _job?.cancel()
        _job = null
    }

    /**
     * 网络是否可用(MainThread)
     * @param isAvailable true-网络可用；false-网络不可用
     */
    abstract fun onChange(isAvailable: Boolean)
}

/**
 * 如果网络可用，直接返回；如果网络不可用，会挂起直到网络可用。
 */
suspend fun fNetworkAvailableAwait() {
    if (FNetwork.isAvailable) return
    suspendCancellableCoroutine { continuation ->
        object : FNetworkAvailableObserver() {
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