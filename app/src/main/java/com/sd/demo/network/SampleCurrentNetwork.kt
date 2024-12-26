package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.theme.AppTheme
import com.sd.lib.network.FNetwork
import com.sd.lib.network.NetworkState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SampleCurrentNetwork : ComponentActivity() {
  private var _flowJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        ContentView { checked ->
          handleCheckedChange(checked)
        }
      }
    }
  }

  private fun handleCheckedChange(checked: Boolean) {
    _flowJob?.cancel()
    if (checked) {
      _flowJob = lifecycleScope.launch {
        // 监听当前网络
        FNetwork.currentNetworkFlow.collect { networkState ->
          networkState.log()
        }
      }
    }
  }
}

@Composable
private fun ContentView(
  modifier: Modifier = Modifier,
  onCheckedChange: (Boolean) -> Unit,
) {
  var checked by remember { mutableStateOf(false) }
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Switch(
      checked = checked,
      onCheckedChange = {
        checked = it
        onCheckedChange(it)
      },
    )
  }
}

private fun NetworkState.log() {
  val wifiOrCellular = when {
    isWifi -> "Wifi"
    isCellular -> "Cellular"
    else -> "None"
  }
  logMsg { "$wifiOrCellular $this" }
}