package com.sd.lib.network

import com.sd.lib.ctx.fContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

object FNetwork {
    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _networksCallback = NetworksCallback(fContext)

    /** 监听所有网络 */
    val networksFlow: Flow<List<NetworkState>> get() = _networksCallback.networksFlow

    /** 当前网络 */
    val currentNetwork: NetworkState get() = _networksCallback.currentNetwork

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState> get() = _networksCallback.currentNetworkFlow

    init {
        // 注册观察者
        _scope.launch {
            _networksCallback.register()
        }
    }
}