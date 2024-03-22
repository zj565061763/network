package com.sd.lib.network

import com.sd.lib.ctx.fContext
import kotlinx.coroutines.flow.Flow

object FNetwork {
    private val _networksCallback = NetworksCallback(fContext)

    /** 当前网络 */
    val currentNetwork: NetworkState
        get() = _networksCallback.currentNetwork

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState>
        get() = _networksCallback.currentNetworkFlow

    /** 监听所有网络 */
    val allNetworksFlow: Flow<List<NetworkState>>
        get() = _networksCallback.allNetworksFlow

    init {
        _networksCallback.register()
    }
}