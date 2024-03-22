package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.SampleNetworkObserverBinding
import com.sd.lib.network.FNetworkObserver
import com.sd.lib.network.NetworkState

class SampleNetworkObserver : AppCompatActivity() {
    private val _binding by lazy { SampleNetworkObserverBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnRegister.setOnClickListener {
            _observer.register()
        }
        _binding.btnUnregister.setOnClickListener {
            _observer.unregister()
        }
    }

    private val _observer = object : FNetworkObserver() {
        override fun onChange(networkState: NetworkState) {
            val wifiOrCellular = when {
                networkState.isWifi() -> "Wifi"
                networkState.isCellular() -> "Cellular"
                else -> "None"
            }

            logMsg {
                """
                    $networkState
                    $wifiOrCellular
                    isConnected:${networkState.isConnected()}
                    isAvailable:${networkState.isAvailable()}
                """.trimIndent()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _observer.unregister()
    }
}