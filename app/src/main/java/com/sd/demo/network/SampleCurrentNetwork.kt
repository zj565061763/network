package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.databinding.SampleCurrentNetworkBinding
import com.sd.lib.network.FNetwork
import com.sd.lib.network.FNetworkObserver
import com.sd.lib.network.NetworkState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SampleCurrentNetwork : AppCompatActivity() {
    private val _binding by lazy { SampleCurrentNetworkBinding.inflate(layoutInflater) }

    private var _flowJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.switchFlow.setOnCheckedChangeListener { _, isChecked ->
            _flowJob?.cancel()
            if (isChecked) {
                _flowJob = lifecycleScope.launch {
                    // 监听当前网络Flow
                    FNetwork.currentNetworkFlow.collect { networkState ->
                        log(networkState, "flow")
                    }
                }
            }
        }

        _binding.switchObserver.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 注册观察者
                _observer.register()
            } else {
                // 取消注册观察者
                _observer.unregister()
            }
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
                    $tag
                    $networkState
                    $wifiOrCellular
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