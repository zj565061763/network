package com.sd.lib.network

/**
 * 网络状态
 */
data class NetworkState(
    /** 网络类型 */
    val networkType: NetworkType,

    /** 网络Id */
    val netId: String,

    /** 网络是否可用 */
    val isAvailable: Boolean,
) {
    /**
     * 网络是否已连接，
     * 注意：已连接不代表网络可用
     */
    fun isConnected(): Boolean = this.networkType != NetworkType.None
}

enum class NetworkType {
    /** 网络不可用 */
    None,

    /** wifi网络 */
    Wifi,

    /** 手机网络 */
    Cellular,

    /** 其他网络 */
    Other,
}

internal val NetworkStateNone = NetworkState(
    networkType = NetworkType.None,
    netId = "",
    isAvailable = false,
)