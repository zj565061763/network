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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

enum class NetworkType {
    /** 未知 */
    Unknown,

    /** 网络不可用 */
    None,

    /** wifi网络 */
    Wifi,

    /** 手机网络 */
    Cellular,

    /** 其他网络 */
    Other,
}

object FNetwork {
    private val _scope = MainScope()
    private val _connectivityManager = fContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networksObserver = NetworksObserver()

    private val _isAvailableFlow = MutableStateFlow<Boolean?>(null)
    private val _networkTypeFlow = MutableStateFlow<NetworkType?>(null)

    /** 网络是否可用 */
    val isAvailable: Boolean get() = _connectivityManager.libNetworkIsAvailable()

    /** 监听网络是否可用 */
    val isAvailableFlow: Flow<Boolean> get() = _isAvailableFlow.filterNotNull()

    /** 监听网络类型 */
    val networkTypeFlow: Flow<NetworkType> get() = _networkTypeFlow.filterNotNull()

    init {
        // 获取网络是否可用
        _scope.launch {
            _networksObserver.networksFlow
                .map { it.isNotEmpty() }
                .distinctUntilChanged()
                .collect {
                    _isAvailableFlow.value = it
                }
        }

        // 获取网络类型
        _scope.launch(Dispatchers.IO) {
            _networksObserver.networksFlow
                .map { it.size }
                .distinctUntilChanged()
                .collectLatest { size ->
                    _networkTypeFlow.value = if (size > 0) {
                        _connectivityManager.libNetworkType()
                    } else {
                        NetworkType.None
                    }
                }
        }

        // 注册观察者
        _scope.launch {
            while (true) {
                try {
                    _networksObserver.register(_connectivityManager)
                    break
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    _networkTypeFlow.value = NetworkType.Unknown
                    delay(60.seconds)
                }
            }
        }
    }
}

private class NetworksObserver {
    private val _networks: MutableSet<Network> = hashSetOf()
    private val _networksFlow = MutableStateFlow<List<Network>?>(null)

    val networksFlow: Flow<List<Network>> get() = _networksFlow.filterNotNull()

    @Throws(RuntimeException::class)
    fun register(manager: ConnectivityManager) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        manager.registerNetworkCallback(request, _networkCallback)
    }

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (_networks.add(network)) {
                _networksFlow.value = _networks.toList()
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            if (_networks.remove(network)) {
                _networksFlow.value = _networks.toList()
            }
        }
    }
}

/**
 * 网络是否可用
 */
private fun ConnectivityManager.libNetworkIsAvailable(): Boolean {
    return getNetworkCapabilities(activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ?: false
}

/**
 * 网络类型
 */
private fun ConnectivityManager.libNetworkType(): NetworkType {
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return NetworkType.None
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.Wifi
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.Cellular
        else -> NetworkType.Other
    }
}