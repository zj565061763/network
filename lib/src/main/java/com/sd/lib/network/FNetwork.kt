package com.sd.lib.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@SuppressLint("StaticFieldLeak")
object FNetwork {
   /** 当前网络 */
   val currentNetwork: NetworkState
      get() = _networksConnectivity.currentNetwork

   /** 监听当前网络 */
   val currentNetworkFlow: Flow<NetworkState>
      get() = _networksConnectivity.currentNetworkFlow

   /** 监听所有网络 */
   val allNetworksFlow: Flow<List<NetworkState>>
      get() = _networksConnectivity.allNetworksFlow

   @Volatile
   private var _context: Context? = null

   private val _networksConnectivity by lazy {
      val context = _context ?: error("You should call FNetwork.init() before this.")
      val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      NetworksConnectivity(manager).also { it.init() }
   }

   /**
    * 初始化
    */
   fun init(context: Context) {
      context.applicationContext?.let { appContext ->
         _context = appContext
      }
   }
}

/**
 * 如果满足[condition]，直接返回，否则挂起直到满足[condition]
 */
suspend fun fNetworkAwait(
   condition: (NetworkState) -> Boolean = { it.isConnected() },
) {
   if (condition(FNetwork.currentNetwork)) return
   FNetwork.currentNetworkFlow.first { networkState ->
      condition(networkState)
   }
}