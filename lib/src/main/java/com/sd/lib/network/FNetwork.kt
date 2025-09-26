package com.sd.lib.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@SuppressLint("StaticFieldLeak")
object FNetwork {
  /** 监听网络是否已连接 */
  val isConnectedFlow: Flow<Boolean> by lazy {
    allNetworksFlow.map { it.isNotEmpty() }.distinctUntilChanged()
  }

  /** 监听当前网络 */
  val currentNetworkFlow: Flow<NetworkState>
    get() = _networkConnectivity.networkFlow

  /** 监听所有网络 */
  val allNetworksFlow: Flow<List<NetworkState>>
    get() = _networksConnectivity.networksFlow

  @Volatile
  private var _context: Context? = null

  private val _connectivityManager by lazy {
    val context = _context ?: error("You should call FNetwork.init() before this.")
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  private val _networkConnectivity by lazy { NetworkConnectivity(_connectivityManager) }
  private val _networksConnectivity by lazy { NetworksConnectivity(_connectivityManager) }

  /**
   * 默认在主进程自动初始化[LibInitializer]，
   * 如果要在其他进程使用，需要在其他进程手动初始化。
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

/** 如果无网络则[debounce]超时[timeoutMillis]毫秒 */
@OptIn(FlowPreview::class)
fun Flow<NetworkState>.debounceNoneNetwork(timeoutMillis: Long = 500): Flow<NetworkState> {
  return debounce { if (it == NetworkStateNone) timeoutMillis else 0 }
    .distinctUntilChanged()
}