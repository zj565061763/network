package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.sd.lib.network.awaitNetworkConnected
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SampleWaitNetwork : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    var count = 0
    lifecycleScope.launch {
      while (true) {
        awaitNetworkConnected()
        delay(500)
        count++
        logMsg { count.toString() }
      }
    }
  }
}