package com.sd.lib.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 监听当前可用的网络类型
 */
abstract class FNetworkTypeObserver {
    private val _scope = MainScope()
    private var _job: Job? = null

    /**
     * 注册
     */
    @Synchronized
    fun register() {
        _job?.let { return }
        _job = _scope.launch {
            FNetwork.networkTypeFlow.collect {
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
     * 当前可用的网络类型(MainThread)
     */
    abstract fun onChange(networkType: NetworkType)
}