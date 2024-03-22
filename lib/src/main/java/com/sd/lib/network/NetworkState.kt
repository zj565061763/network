package com.sd.lib.network

import android.net.NetworkCapabilities

/**
 * 网络状态
 */
data class NetworkState(
    /** 网络Id */
    val netId: String,

    /** [NetworkCapabilities.TRANSPORT_WIFI] */
    val transportWifi: Boolean,

    /** [NetworkCapabilities.TRANSPORT_CELLULAR] */
    val transportCellular: Boolean,

    /** [NetworkCapabilities.NET_CAPABILITY_INTERNET] */
    val netCapabilityInternet: Boolean,

    /** [NetworkCapabilities.NET_CAPABILITY_VALIDATED] */
    val netCapabilityValidated: Boolean,
) {
    /**
     * 是否Wifi网络
     */
    fun isWifi(): Boolean = this.transportWifi

    /**
     * 是否手机网络
     */
    fun isCellular(): Boolean = this.transportCellular

    /**
     * 网络是否已连接，注意：已连接不代表网络可用
     */
    fun isConnected(): Boolean = this.netCapabilityInternet

    /**
     * 网络是否可用
     */
    fun isAvailable(): Boolean = this.netCapabilityValidated
}

internal val NetworkStateNone = NetworkState(
    netId = "",
    transportWifi = false,
    transportCellular = false,
    netCapabilityInternet = false,
    netCapabilityValidated = false,
)