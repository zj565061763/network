package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.databinding.SampleAllNetworksBinding
import com.sd.lib.network.FNetwork
import kotlinx.coroutines.launch

class SampleAllNetworks : AppCompatActivity() {
    private val _binding by lazy { SampleAllNetworksBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        lifecycleScope.launch {
            FNetwork.allNetworksFlow.collect {
                logMsg {
                    it.joinToString(separator = "\n")
                }
            }
        }
    }
}