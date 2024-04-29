package com.sd.lib.network

import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

object FNetwork {
    /** 当前网络 */
    val currentNetwork: NetworkState
        get() = getNetworksConnectivity().currentNetwork

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState>
        get() = getNetworksConnectivity().currentNetworkFlow

    /** 监听所有网络 */
    val allNetworksFlow: Flow<List<NetworkState>>
        get() = getNetworksConnectivity().allNetworksFlow

    @Volatile
    private var _networksConnectivity: NetworksConnectivity? = null

    /**
     * 初始化
     */
    fun init(context: Context) {
        if (_networksConnectivity != null) return
        synchronized(this@FNetwork) {
            if (_networksConnectivity == null) {
                _networksConnectivity = NetworksConnectivity(context)
                _networksConnectivity!!.init()
            }
        }
    }

    private fun getNetworksConnectivity(): NetworksConnectivity {
        return _networksConnectivity ?: synchronized(this@FNetwork) {
            checkNotNull(_networksConnectivity) { "You should call FNetwork.init() before this." }
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
     * 当前网络状态变化(MainThread)
     */
    protected abstract fun onChange(networkState: NetworkState)
}

/**
 * 如果满足[condition]，直接返回，否则挂起直到满足[condition]
 */
suspend fun fNetworkAwait(
    condition: (NetworkState) -> Boolean = { it.isConnected() },
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