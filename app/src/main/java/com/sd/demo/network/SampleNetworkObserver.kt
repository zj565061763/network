package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.SampleNetworkObserverBinding
import com.sd.lib.network.FNetworkAvailableObserver

class SampleNetworkObserver : AppCompatActivity() {
    private val _binding by lazy { SampleNetworkObserverBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnRegister.setOnClickListener {
            _availableObserver.register()
        }
        _binding.btnUnregister.setOnClickListener {
            _availableObserver.unregister()
        }
    }

    private val _availableObserver = object : FNetworkAvailableObserver() {
        override fun onChange(isAvailable: Boolean) {
            logMsg { "onChange:$isAvailable" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _availableObserver.unregister()
    }
}