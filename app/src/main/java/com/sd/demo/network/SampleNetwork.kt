package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.sd.lib.network.FNetwork
import com.sd.lib.network.NetworkState
import com.sd.lib.network.debounceNoneNetwork
import kotlinx.coroutines.launch

/**
 * 监听当前网络状态
 */
class SampleNetwork : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      FNetwork.networkFlow
        .debounceNoneNetwork()
        .collect { networkState -> networkState.log() }
    }
  }
}

private fun NetworkState.log() {
  val wifiOrCellular = when {
    isWifi -> "Wifi"
    isCellular -> "Cellular"
    isEthernet -> "Ethernet"
    else -> "None"
  }
  logMsg { "$wifiOrCellular $this" }
}