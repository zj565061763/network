package com.sd.lib.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal class NetworksConnectivity(
    context: Context
) {
    private val _connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _networks = mutableMapOf<Network, NetworkState>()

    private val _currentNetworkFlow = MutableStateFlow<NetworkState?>(null)
    private val _allNetworksFlow = MutableStateFlow<List<NetworkState>?>(null)

    /** 当前网络 */
    val currentNetwork: NetworkState
        get() = _connectivityManager.currentNetworkState()

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState>
        get() = _currentNetworkFlow.filterNotNull()

    /** 监听所有网络 */
    val allNetworksFlow: Flow<List<NetworkState>>
        get() = _allNetworksFlow.filterNotNull()

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            _networks.remove(network)
            _allNetworksFlow.value = _networks.values.toList()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            _networks[network] = newNetworkState(network, networkCapabilities)
            _allNetworksFlow.value = _networks.values.toList()
        }
    }

    /**
     * 初始化
     */
    fun init() {
        _scope.launch {
            registerNetworkCallback()
        }
        _scope.launch {
            allNetworksFlow.collectLatest { list ->
                _currentNetworkFlow.value = filterCurrentNetwork(list)
            }
        }
    }

    /**
     * 注册网络监听
     */
    private suspend fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        while (true) {
            val register = try {
                _connectivityManager.registerNetworkCallback(request, _networkCallback)
                true
            } catch (e: RuntimeException) {
                e.printStackTrace()
                false
            }

            val list = _connectivityManager.currentNetworkState().let { currentNetworkState ->
                if (currentNetworkState.netId.isEmpty()) {
                    emptyList()
                } else {
                    listOf(currentNetworkState)
                }
            }

            if (register) {
                _allNetworksFlow.compareAndSet(null, list)
                break
            } else {
                _allNetworksFlow.value = list
                delay(1_000)
                continue
            }
        }
    }

    /**
     * 筛选当前网络状态
     */
    private suspend fun filterCurrentNetwork(list: List<NetworkState>): NetworkState {
        if (list.isEmpty()) return NetworkStateNone
        if (list.size == 1) return list.first()
        while (true) {
            val target = list.find { it.netId == _connectivityManager.activeNetwork?.netId() }
            if (target != null) {
                return target
            } else {
                delay(1_000)
                continue
            }
        }
    }
}

private fun ConnectivityManager.currentNetworkState(): NetworkState {
    val network = this.activeNetwork ?: return NetworkStateNone
    val capabilities = this.getNetworkCapabilities(network) ?: return NetworkStateNone
    return newNetworkState(network, capabilities)
}

private fun newNetworkState(
    network: Network,
    networkCapabilities: NetworkCapabilities,
): NetworkState {
    return NetworkState(
        netId = network.netId(),
        transportWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
        transportCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
        netCapabilityInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
        netCapabilityValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
    )
}

private fun Network.netId(): String = this.toString()