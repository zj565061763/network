package com.sd.lib.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@SuppressLint("StaticFieldLeak")
object FNetwork {
  /** 监听当前网络 */
  val currentNetworkFlow: Flow<NetworkState>
    get() = _currentNetwork.networkFlow

  /** 监听所有网络 */
  val allNetworksFlow: Flow<List<NetworkState>>
    get() = _allNetworks.networksFlow

  @Volatile
  private var _context: Context? = null

  private val _connectivityManager by lazy {
    val context = _context ?: error("You should call FNetwork.init() before this.")
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  private val _currentNetwork by lazy { NetworkConnectivity(_connectivityManager) }
  private val _allNetworks by lazy { NetworksConnectivity(_connectivityManager) }

  /**
   * 默认在主进程自动初始化，如果要在其他进程使用，需要在其他进程手动初始化。
   */
  @JvmStatic
  fun init(context: Context) {
    context.applicationContext?.also { appContext ->
      _context = appContext
    }
  }

  /** 获取当前网络 */
  @JvmStatic
  fun getCurrentNetwork(): NetworkState {
    return _connectivityManager.currentNetworkState() ?: NetworkStateNone
  }
}

/**
 * 如果当前网络不满足[condition]，则挂起直到满足[condition]，默认[condition]为网络已连接
 * @return true-调用时已经满足[condition]；false-调用时还不满足[condition]，挂起等待之后满足[condition]
 */
suspend fun fAwaitNetwork(
  condition: (NetworkState) -> Boolean = { it.isConnected },
): Boolean {
  if (condition(FNetwork.getCurrentNetwork())) return true
  FNetwork.currentNetworkFlow.first { condition(it) }
  return false
}