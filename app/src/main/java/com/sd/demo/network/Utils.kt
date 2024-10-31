package com.sd.demo.network

import com.sd.lib.network.NetworkState

/**
 * 打印网络信息
 */
fun NetworkState.log() {
   val wifiOrCellular = when {
      isWifi() -> "Wifi"
      isCellular() -> "Cellular"
      else -> "None"
   }

   logMsg {
      """
                    $wifiOrCellular
                    netId:${netId}
                    isConnected:${isConnected()}
                    isAvailable:${isAvailable()}
                     
                """.trimIndent()
   }
}