package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.SampleNetworkObserverBinding
import com.sd.lib.network.FNetworkObserver

class SampleNetworkObserver : AppCompatActivity() {
    private val _binding by lazy { SampleNetworkObserverBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnRegister.setOnClickListener {
            logMsg { "click register" }
            _observer.register()
        }
        _binding.btnUnregister.setOnClickListener {
            logMsg { "click unregister" }
            _observer.unregister()
        }
    }

    private val _observer = object : FNetworkObserver() {
        override fun onChange(isAvailable: Boolean) {
            logMsg { "onChange:$isAvailable" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _observer.unregister()
    }
}