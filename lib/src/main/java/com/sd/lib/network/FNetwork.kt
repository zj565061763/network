package com.sd.lib.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.sd.lib.ctx.fContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

object FNetwork {
    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connectivityManager = fContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStateFlow = MutableStateFlow<NetworkState?>(null)

    private val _networks: MutableSet<Network> = Collections.synchronizedSet(hashSetOf())
    private var _updateJob: Job? = null

    /** 网络状态 */
    val networkState: NetworkState
        get() {
            updateNetworkState()
            return _connectivityManager.networkState()
        }

    /** 监听网络状态 */
    val networkStateFlow: Flow<NetworkState>
        get() = _networkStateFlow.filterNotNull()

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _networks.add(network)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _networks.remove(network)
            updateNetworkState()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateNetworkState()
        }
    }

    private fun updateNetworkState() {
        _updateJob?.cancel()
        _updateJob = _scope.launch {
            while (true) {
                yield()
                if (_networks.isEmpty()) {
                    _networkStateFlow.value = NetworkStateNone
                    break
                } else {
                    if (_connectivityManager.activeNetwork == null) {
                        delay(1.seconds)
                        continue
                    } else {
                        updateFlow()
                        break
                    }
                }
            }
        }
    }

    private fun updateFlow() {
        _networkStateFlow.value = _connectivityManager.networkState()
    }

    init {
        // 注册观察者
        _scope.launch {
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()?.let { request ->
                    while (true) {
                        try {
                            _connectivityManager.registerNetworkCallback(request, _networkCallback)
                            break
                        } catch (e: RuntimeException) {
                            e.printStackTrace()
                            delay(1.seconds)
                        } finally {
                            updateFlow()
                        }
                    }
                }
        }
    }
}

/**
 * 网络是否可用
 */
private fun ConnectivityManager.networkState(
    network: Network? = null,
): NetworkState {
    val realNetwork = network ?: activeNetwork ?: return NetworkStateNone
    val capabilities = getNetworkCapabilities(realNetwork) ?: return NetworkStateNone

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
        netId = realNetwork.toString(),
    )
}