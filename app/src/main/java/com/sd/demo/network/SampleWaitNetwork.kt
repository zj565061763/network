package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.theme.AppTheme
import com.sd.lib.network.fAwaitNetwork
import kotlinx.coroutines.launch
import java.util.UUID

class SampleWaitNetwork : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        ContentView(
          onClickLaunch = {
            lifecycleScope.launch {
              launchNetwork()
            }
          }
        )
      }
    }
  }

  private suspend fun launchNetwork() {
    val uuid = UUID.randomUUID().toString()
    runCatching {
      logMsg { "$uuid start" }
      fAwaitNetwork()
    }.onSuccess {
      logMsg { "$uuid onSuccess" }
    }.onFailure {
      logMsg { "$uuid onFailure $it" }
    }
  }
}

@Composable
private fun ContentView(
  modifier: Modifier = Modifier,
  onClickLaunch: () -> Unit,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Button(onClick = onClickLaunch) {
      Text(text = "launch")
    }
  }
}