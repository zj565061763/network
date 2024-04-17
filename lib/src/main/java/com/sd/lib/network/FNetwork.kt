package com.sd.lib.network

import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

object FNetwork {
    @Volatile
    private var _callback: NetworksCallback? = null

    /** 当前网络 */
    val currentNetwork: NetworkState
        get() = getCallback().currentNetwork

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState>
        get() = getCallback().currentNetworkFlow

    /** 监听所有网络 */
    val allNetworksFlow: Flow<List<NetworkState>>
        get() = getCallback().allNetworksFlow

    /**
     * 初始化
     */
    fun init(context: Context) {
        if (_callback != null) return
        synchronized(this@FNetwork) {
            if (_callback == null) {
                _callback = NetworksCallback(context.applicationContext)
                _callback!!.init()
            }
        }
    }

    private fun getCallback(): NetworksCallback {
        return _callback ?: synchronized(this@FNetwork) {
            checkNotNull(_callback) { "You should call FNetwork.init() before this." }
        }
    }
}

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
        if (_job == null) {
            _job = _scope.launch {
                FNetwork.currentNetworkFlow.collect {
                    onChange(it)
                }
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
    protected abstract fun onChange(networkState: NetworkState)
}

/**
 * 如果满足[condition]，直接返回，否则挂起直到满足[condition]
 */
suspend fun fNetworkAwait(
    condition: (NetworkState) -> Boolean = { it.isConnected() }
) {
    if (condition(FNetwork.currentNetwork)) return
    suspendCancellableCoroutine { continuation ->
        object : FNetworkObserver() {
            override fun onChange(networkState: NetworkState) {
                if (condition(networkState)) {
                    unregister()
                    continuation.resumeWith(Result.success(Unit))
                }
            }
        }.let { observer ->
            observer.register()
            continuation.invokeOnCancellation { observer.unregister() }
        }
    }
}