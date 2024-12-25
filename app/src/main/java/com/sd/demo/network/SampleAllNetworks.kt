package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.theme.AppTheme
import com.sd.lib.network.FNetwork
import com.sd.lib.network.NetworkState
import kotlinx.coroutines.launch

class SampleAllNetworks : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      // 监听所有网络
      FNetwork.allNetworksFlow.collect { list ->
        logMsg {
          "size:${list.size}\n${list.joinToString(separator = "\n")}"
        }
      }
    }

    setContent {
      AppTheme {
        ContentView()
      }
    }
  }
}

@Composable
private fun ContentView(
  modifier: Modifier = Modifier,
) {
  val list by FNetwork.allNetworksFlow.collectAsStateWithLifecycle(emptyList())

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(10.dp),
  ) {
    items(
      items = list,
      key = { it.id },
    ) { item ->
      ItemView(networkState = item)
    }
  }
}

@Composable
private fun ItemView(
  modifier: Modifier = Modifier,
  networkState: NetworkState,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Text(
      text = networkState.toString(),
      modifier = Modifier.padding(10.dp),
    )
  }
}