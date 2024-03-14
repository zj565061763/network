package com.sd.lib.network

import com.sd.lib.ctx.fContext

abstract class FNetworkObserver {

    private val _observer = networkObserver(
        context = fContext,
        onChange = { onChange(it) },
    )

    /**
     * 注册
     * @param notify 注册成功之后是否立即通知一次回调，默认true
     */
    @JvmOverloads
    fun register(notify: Boolean = true) {
        _observer.register(notify = notify)
    }

    /**
     * 取消注册
     */
    fun unregister() {
        _observer.unregister()
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
        fun isNetworkAvailable(): Boolean {
            return libIsNetworkAvailable(fContext)
        }
    }
}