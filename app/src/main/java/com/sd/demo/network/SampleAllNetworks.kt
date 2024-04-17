package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.sd.lib.network.FNetwork
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
    }
}