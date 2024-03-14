package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.databinding.SampleAwaitNetworkBinding
import com.sd.lib.network.fAwaitNetworkAvailable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class SampleAwaitNetwork : AppCompatActivity() {
    private val _binding by lazy { SampleAwaitNetworkBinding.inflate(layoutInflater) }

    private var _jobs = mutableListOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnLaunch.setOnClickListener {
            lifecycleScope.launch {
                launchNetWorkAvailable()
            }.also {
                _jobs.add(it)
            }
        }
        _binding.btnCancel.setOnClickListener {
            _jobs.forEach { it.cancel() }
            _jobs.clear()
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
}