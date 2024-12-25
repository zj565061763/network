package com.sd.lib.network

import android.net.NetworkCapabilities

interface NetworkState {
  /** 网络Id */
  val id: String

  /** 是否Wifi网络 */
  val isWifi: Boolean

  /** 是否手机网络 */
  val isCellular: Boolean

  /** 网络是否已连接 */
  val isConnected: Boolean
}

internal data class NetworkStateModel(
  /** 网络Id */
  val netId: String,
  /** [NetworkCapabilities.TRANSPORT_WIFI] */
  val transportWifi: Boolean,
  /** [NetworkCapabilities.TRANSPORT_CELLULAR] */
  val transportCellular: Boolean,
  /** [NetworkCapabilities.NET_CAPABILITY_INTERNET] */
  val netCapabilityInternet: Boolean,
) : NetworkState {

  override val id: String
    get() = netId

  override val isWifi: Boolean
    get() = transportWifi

  override val isCellular: Boolean
    get() = transportCellular

  override val isConnected: Boolean
    get() = netCapabilityInternet
}

internal val NetworkStateNone: NetworkState = NetworkStateModel(
  netId = "",
  transportWifi = false,
  transportCellular = false,
  netCapabilityInternet = false,
)