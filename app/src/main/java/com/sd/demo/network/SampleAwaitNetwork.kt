package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.databinding.SampleAwaitNetworkBinding
import com.sd.lib.network.fNetworkAvailableAwait
import kotlinx.coroutines.launch
import java.util.UUID

class SampleAwaitNetwork : AppCompatActivity() {
    private val _binding by lazy { SampleAwaitNetworkBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnLaunch.setOnClickListener {
            lifecycleScope.launch {
                launchNetWorkAvailable()
            }
        }
    }

    private suspend fun launchNetWorkAvailable() {
        val uuid = UUID.randomUUID().toString()
        logMsg { "start $uuid" }
        fNetworkAvailableAwait()
        logMsg { "finish $uuid" }
    }
}