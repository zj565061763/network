package com.sd.lib.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

abstract class FNetworkObserver {
    private val _scope = MainScope()
    private var _job: Job? = null

    /**
     * 注册
     * @param notify 注册成功之后是否立即通知一次回调，默认true
     */
    @JvmOverloads
    fun register(notify: Boolean = true) {
        synchronized(this@FNetworkObserver) {
            _job?.let { return }
            _job = _scope.launch {
                fIsNetworkAvailableFlow
                    .let { if (notify) it else it.drop(1) }
                    .collect { onChange(it) }
            }
        }
    }

    /**
     * 取消注册
     */
    fun unregister() {
        synchronized(this@FNetworkObserver) {
            _job?.cancel()
            _job = null
        }
    }

    /**
     * 网络是否可用(MainThread)
     * @param isAvailable true-网络可用；false-网络不可用
     */
    abstract fun onChange(isAvailable: Boolean)

    companion object {
        /**
         * 网络是否可用
         */
        @JvmStatic
        fun isNetworkAvailable(): Boolean = fIsNetworkAvailable
    }
}