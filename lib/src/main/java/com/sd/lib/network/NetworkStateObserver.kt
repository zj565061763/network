package com.sd.lib.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 监听当前网络
 */
abstract class FNetworkObserver {
    private val _scope = MainScope()
    private var _job: Job? = null

    /**
     * 注册
     */
    @Synchronized
    fun register() {
        _job?.let { return }
        _job = _scope.launch {
            FNetwork.currentNetworkFlow.collect {
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
     * 网络状态变化(MainThread)
     */
    abstract fun onChange(networkState: NetworkState)
}

/**
 * 如果网络已连接，直接返回，否则挂起直到网络已连接
 */
suspend fun fNetworkConnectedAwait() {
    if (FNetwork.currentNetwork.isConnected()) return
    suspendCancellableCoroutine { continuation ->
        object : FNetworkObserver() {
            override fun onChange(networkState: NetworkState) {
                if (networkState.isConnected()) {
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