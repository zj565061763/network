package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.databinding.SampleNetworkObserverBinding
import com.sd.lib.network.FNetwork
import com.sd.lib.network.FNetworkObserver
import com.sd.lib.network.NetworkState
import kotlinx.coroutines.launch

class SampleNetworkObserver : AppCompatActivity() {
    private val _binding by lazy { SampleNetworkObserverBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnRegister.setOnClickListener {
            // 注册观察者
            _observer.register()
        }
        _binding.btnUnregister.setOnClickListener {
            // 取消注册观察者
            _observer.unregister()
        }

        lifecycleScope.launch {
            // 监听当前网络Flow
            FNetwork.currentNetworkFlow.collect { networkState ->
                log(networkState, "flow")
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