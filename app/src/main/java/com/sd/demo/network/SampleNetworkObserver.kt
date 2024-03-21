package com.sd.demo.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.SampleNetworkObserverBinding
import com.sd.lib.network.FNetworkAvailableObserver
import com.sd.lib.network.FNetworkTypeObserver
import com.sd.lib.network.NetworkType

class SampleNetworkObserver : AppCompatActivity() {
    private val _binding by lazy { SampleNetworkObserverBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnRegister.setOnClickListener {
            _availableObserver.register()
            _typeObserver.register()
        }
        _binding.btnUnregister.setOnClickListener {
            _availableObserver.unregister()
            _typeObserver.unregister()
        }
    }

    private val _availableObserver = object : FNetworkAvailableObserver() {
        override fun onChange(isAvailable: Boolean) {
            logMsg { "isAvailable:$isAvailable" }
        }
    }

    private val _typeObserver = object : FNetworkTypeObserver() {
        override fun onChange(networkType: NetworkType) {
            logMsg { "networkType:$networkType" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _availableObserver.unregister()
    }
}