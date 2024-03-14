package com.sd.lib.network

import com.sd.lib.ctx.fContext

abstract class FNetworkObserver {

    private val _observer = networkObserver(
        context = fContext,
        onChange = { onChange(it) },
    )

    /**
     * 注册
     * @param notify 注册之后是否立即通知一次回调，默认true
     */
    @JvmOverloads
    fun register(notify: Boolean = true) {
        if (_observer.register()) {
            if (notify) {
                _observer.notifyCallback()
            }
        }
    }

    /**
     * 取消注册
     */
    fun unregister() {
        _observer.unregister()
    }

    /**
     * 网络是否可用(MainThread)
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