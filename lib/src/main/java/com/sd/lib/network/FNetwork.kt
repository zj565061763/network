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
      NetworksConnectivity(manager)
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
 * 如果当前网络不满足[condition]，则挂起直到满足[condition]，默认[condition]为网络已连接
 * @return true-调用时已经满足[condition]；false-调用时还不满足[condition]，挂起等待之后满足[condition]
 */
suspend fun fNetwork(
   condition: (NetworkState) -> Boolean = { it.isConnected },
): Boolean {
   if (condition(FNetwork.currentNetwork)) return true
   FNetwork.currentNetworkFlow.first { condition(it) }
   return false
}