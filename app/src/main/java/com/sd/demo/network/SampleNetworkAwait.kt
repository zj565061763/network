package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.databinding.SampleNetworkAwaitBinding
import com.sd.lib.network.fNetworkAwait
import kotlinx.coroutines.launch
import java.util.UUID

class SampleNetworkAwait : AppCompatActivity() {
    private val _binding by lazy { SampleNetworkAwaitBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnLaunch.setOnClickListener {
            lifecycleScope.launch {
                launchNetWorkAwait()
            }
        }
    }

    private suspend fun launchNetWorkAwait() {
        val uuid = UUID.randomUUID().toString()
        logMsg { "start $uuid" }
        fNetworkAwait()
        logMsg { "finish $uuid" }
    }
}