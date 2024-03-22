package com.sd.lib.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

internal class NetworksCallback(
    context: Context
) {
    private val _connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networks: MutableMap<Network, NetworkState> = hashMapOf()
    private val _networksFlow = MutableStateFlow<List<NetworkState>?>(null)

    /** 监听所有网络 */
    val networksFlow: Flow<List<NetworkState>>
        get() = _networksFlow.filterNotNull()

    /** 当前网络 */
    val currentNetwork: NetworkState
        get() = _connectivityManager.networkState()

    /** 监听当前网络 */
    val currentNetworkFlow: Flow<NetworkState>
        get() = networksFlow
            .map { list ->
                list.firstOrNull {
                    it.netId == _connectivityManager.activeNetwork.toString()
                } ?: NetworkStateNone
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            _networks.remove(network)
            _networksFlow.value = _networks.values.toList()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            _networks[network] = network.toNetworkState(networkCapabilities)
            _networksFlow.value = _networks.values.toList()
        }
    }

    private fun updateDefaultNetwork() {
        val oldList = _networksFlow.value
        if (oldList.isNullOrEmpty() || oldList.size == 1) {
            val newList = listOf(_connectivityManager.networkState())
            _networksFlow.compareAndSet(oldList, newList)
        }
    }

    suspend fun register() {
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
                updateDefaultNetwork()
            }
        }
    }
}

private fun ConnectivityManager.networkState(): NetworkState {
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