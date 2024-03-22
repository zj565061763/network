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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

internal class NetworksCallback(
    context: Context
) {
    private val _connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _register = AtomicBoolean(false)
    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _networks: MutableMap<Network, NetworkState> = hashMapOf()

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
            _networks[network] = network.toNetworkState(networkCapabilities)
            _allNetworksFlow.value = _networks.values.toList()
        }
    }

    /**
     * 注册回调对象
     */
    fun register() {
        if (_register.compareAndSet(false, true)) {
            _scope.launch {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                while (true) {
                    try {
                        _connectivityManager.registerNetworkCallback(request, _networkCallback)
                        break
                    } catch (e: RuntimeException) {
                        e.printStackTrace()
                        delay(1.seconds)
                    } finally {
                        updateCurrentNetwork()
                    }
                }
            }
            _scope.launch {
                allNetworksFlow.collectLatest { list ->
                    list.filterCurrentNetwork()?.let {
                        _currentNetworkFlow.value = it
                    }
                }
            }
        }
    }

    /**
     * 更新当前网络状态
     */
    private fun updateCurrentNetwork() {
        val oldList = _allNetworksFlow.value
        if (oldList.isNullOrEmpty() || oldList.size == 1) {
            val newList = listOf(_connectivityManager.currentNetworkState())
            _allNetworksFlow.compareAndSet(oldList, newList)
        }
    }

    /**
     * 筛选当前网络状态
     */
    private suspend fun List<NetworkState>.filterCurrentNetwork(): NetworkState? {
        if (this.isEmpty()) return NetworkStateNone
        while (true) {
            val activeNetwork = _connectivityManager.activeNetwork
            if (activeNetwork == null) {
                delay(1.seconds)
                continue
            } else {
                return this.firstOrNull { it.netId == activeNetwork.toString() }
            }
        }
    }
}

private fun ConnectivityManager.currentNetworkState(): NetworkState {
    val network = activeNetwork ?: return NetworkStateNone
    val capabilities = getNetworkCapabilities(network) ?: return NetworkStateNone
    return network.toNetworkState(capabilities)
}

private fun Network.toNetworkState(networkCapabilities: NetworkCapabilities): NetworkState {
    return NetworkState(
        netId = this.toString(),
        transportWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
        transportCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
        netCapabilityInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
        netCapabilityValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
    )
}