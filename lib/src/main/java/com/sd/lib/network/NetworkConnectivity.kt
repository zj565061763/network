package com.sd.lib.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

/** 当前网络 */
internal class NetworkConnectivity(
  manager: ConnectivityManager,
) : BaseNetworkConnectivity(manager) {
  private val _networkFlow = MutableStateFlow<NetworkState?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val networkFlow: Flow<NetworkState>
    get() = _networkFlow.filterNotNull()
      .mapLatest { state ->
        if (state == NetworkStateNone) delay(600)
        state
      }.distinctUntilChanged()

  override fun onRegisterCallback() {
    manager.registerDefaultNetworkCallback(this)
  }

  override fun onRegisterCallbackResult(register: Boolean, networkState: NetworkState?) {
    val state = networkState ?: NetworkStateNone
    if (register) {
      _networkFlow.compareAndSet(null, state)
    } else {
      _networkFlow.value = state
    }
  }

  override fun onLost(network: Network) {
    _networkFlow.value = NetworkStateNone
  }

  override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
    _networkFlow.value = newNetworkState(network, networkCapabilities)
  }
}

/** 所有网络 */
internal class NetworksConnectivity(
  manager: ConnectivityManager,
) : BaseNetworkConnectivity(manager) {
  private val _networks = mutableMapOf<Network, NetworkState>()
  private val _networksFlow = MutableStateFlow<List<NetworkState>?>(null)

  val networksFlow: Flow<List<NetworkState>>
    get() = _networksFlow.filterNotNull()

  override fun onRegisterCallback() {
    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()
    manager.registerNetworkCallback(request, this)
  }

  override fun onRegisterCallbackResult(register: Boolean, networkState: NetworkState?) {
    val list = if (networkState != null) listOf(networkState) else emptyList()
    if (register) {
      _networksFlow.compareAndSet(null, list)
    } else {
      _networksFlow.value = list
    }
  }

  override fun onLost(network: Network) {
    _networks.remove(network)
    _networksFlow.value = _networks.values.toList()
  }

  override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
    _networks[network] = newNetworkState(network, networkCapabilities)
    _networksFlow.value = _networks.values.toList()
  }
}

internal abstract class BaseNetworkConnectivity(
  protected val manager: ConnectivityManager,
) : ConnectivityManager.NetworkCallback() {

  private suspend fun registerNetworkCallback() {
    while (true) {
      val register = try {
        onRegisterCallback()
        true
      } catch (e: RuntimeException) {
        e.printStackTrace()
        false
      }

      val networkState = manager.currentNetworkState()
      onRegisterCallbackResult(register, networkState)

      if (register) {
        break
      } else {
        delay(1_000)
        continue
      }
    }
  }

  protected abstract fun onRegisterCallback()
  protected abstract fun onRegisterCallbackResult(register: Boolean, networkState: NetworkState?)

  init {
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch {
      registerNetworkCallback()
    }
  }
}

internal fun ConnectivityManager.currentNetworkState(): NetworkState? {
  val network = this.activeNetwork ?: return null
  val capabilities = this.getNetworkCapabilities(network) ?: return null
  return newNetworkState(network, capabilities)
}

private fun newNetworkState(
  network: Network,
  networkCapabilities: NetworkCapabilities,
): NetworkState {
  return NetworkStateModel(
    netId = network.toString(),
    transportWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
    transportCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
    netCapabilityInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
  )
}