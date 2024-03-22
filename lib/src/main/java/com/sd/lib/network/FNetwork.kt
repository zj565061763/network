package com.sd.lib.network

import com.sd.lib.ctx.fContext
import kotlinx.coroutines.flow.Flow

object FNetwork {
    private val _callback = NetworksCallback(fContext)

    /** 当前网络 */
    val currentNetwork: NetworkState
        get() = _callback.currentNetwork

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState>
        get() = _callback.currentNetworkFlow

    /** 监听所有网络 */
    val allNetworksFlow: Flow<List<NetworkState>>
        get() = _callback.allNetworksFlow

    init {
        _callback.register()
    }
}