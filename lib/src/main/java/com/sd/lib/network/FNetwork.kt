package com.sd.lib.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.sd.lib.ctx.fContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

object FNetwork {
    private val _scope = MainScope()
    private val _connectivityManager = fContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStateFlow = MutableStateFlow<NetworkState?>(null)

    /** 网络状态 */
    val networkState: NetworkState get() = _connectivityManager.networkState()
    /** 监听网络状态 */
    val networkStateFlow: Flow<NetworkState> get() = _networkStateFlow.filterNotNull()

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        private var _networkCount = 0

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _networkCount++
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            (_networkCount - 1).coerceAtLeast(0).let { count ->
                _networkCount = count
                if (count == 0) {
                    _networkStateFlow.value = NetworkStateNone
                }
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            _networkStateFlow.value = _connectivityManager.networkState()
        }
    }

    init {
        // 注册观察者
        _scope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val request = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()
                    _connectivityManager.registerNetworkCallback(request, _networkCallback)
                    break
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    _networkStateFlow.value = _connectivityManager.networkState()
                    delay(1.seconds)
                }
            }
        }
    }
}

/**
 * 网络是否可用
 */
private fun ConnectivityManager.networkState(): NetworkState {
    val network = activeNetwork ?: return NetworkStateNone
    val capabilities = getNetworkCapabilities(network) ?: return NetworkStateNone

    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
        return NetworkStateNone
    }

    val networkType = when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.Wifi
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.Cellular
        else -> NetworkType.Other
    }

    return NetworkState(
        networkType = networkType,
        isAvailable = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
        netId = network.toString(),
    )
}