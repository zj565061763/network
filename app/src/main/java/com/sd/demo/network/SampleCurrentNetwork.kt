package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.theme.AppTheme
import com.sd.lib.network.FNetwork
import com.sd.lib.network.FNetworkObserver
import com.sd.lib.network.NetworkState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SampleCurrentNetwork : ComponentActivity() {
    private var _flowJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ContentView(
                    onFlowCheckedChange = {
                        handleFlowChangedChange(it)
                    },
                    onObserverCheckedChange = {
                        handleObserverChangedChange(it)
                    },
                )
            }
        }
    }

    private fun handleFlowChangedChange(checked: Boolean) {
        _flowJob?.cancel()
        if (checked) {
            _flowJob = lifecycleScope.launch {
                // 监听当前网络Flow
                FNetwork.currentNetworkFlow.collect { networkState ->
                    log(networkState, "flow")
                }
            }
        }
    }

    private fun handleObserverChangedChange(checked: Boolean) {
        if (checked) {
            // 注册观察者
            _observer.register()
        } else {
            // 取消注册观察者
            _observer.unregister()
        }
    }

    private val _observer = object : FNetworkObserver() {
        override fun onChange(networkState: NetworkState) {
            log(networkState, "observer")
        }
    }

    /**
     * 打印网络信息
     */
    private fun log(networkState: NetworkState, tag: String) {
        val wifiOrCellular = when {
            networkState.isWifi() -> "Wifi"
            networkState.isCellular() -> "Cellular"
            else -> "None"
        }

        logMsg {
            """
                    $tag $wifiOrCellular
                    netId:${networkState.netId}
                    isConnected:${networkState.isConnected()}
                    isAvailable:${networkState.isAvailable()}
                     
                """.trimIndent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消注册观察者
        _observer.unregister()
    }
}

@Composable
private fun ContentView(
    modifier: Modifier = Modifier,
    onFlowCheckedChange: (Boolean) -> Unit,
    onObserverCheckedChange: (Boolean) -> Unit,
) {
    var flowChecked by remember { mutableStateOf(false) }
    var observerChecked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Switch(
            checked = flowChecked,
            onCheckedChange = {
                flowChecked = it
                onFlowCheckedChange(it)
            },
        )

        Spacer(modifier = Modifier.height(30.dp))

        Switch(
            checked = observerChecked,
            onCheckedChange = {
                observerChecked = it
                onObserverCheckedChange(it)
            },
        )
    }
}