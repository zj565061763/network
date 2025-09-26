package com.sd.lib.network

import kotlinx.coroutines.flow.first

/** 如果当前网络未连接，则挂起直到网络连接 */
suspend fun awaitNetworkConnected() {
  FNetwork.isConnectedFlow.first { it }
}

/** 如果当前网络不满足[condition]，则挂起直到满足[condition] */
suspend fun awaitNetwork(condition: (NetworkState) -> Boolean) {
  FNetwork.currentNetworkFlow.first { condition(it) }
}