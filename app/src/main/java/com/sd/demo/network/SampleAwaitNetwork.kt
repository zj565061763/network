package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.SampleAwaitNetworkBinding
import com.sd.lib.coroutine.FScope
import com.sd.lib.network.fAwaitNetworkAvailable
import java.util.UUID

class SampleAwaitNetwork : AppCompatActivity() {
    private val _binding by lazy { SampleAwaitNetworkBinding.inflate(layoutInflater) }

    private val _scope = FScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnLaunch.setOnClickListener {
            _scope.launch {
                launchNetWorkAvailable()
            }
        }
        _binding.btnCancel.setOnClickListener {
            _scope.cancel()
        }
    }

    private suspend fun launchNetWorkAvailable() {
        val uuid = UUID.randomUUID().toString()
        logMsg { "start $uuid" }

        try {
            fAwaitNetworkAvailable()
        } catch (e: Exception) {
            logMsg { "exception $uuid $e" }
            throw e
        }

        logMsg { "finish $uuid" }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }
}