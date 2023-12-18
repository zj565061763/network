package com.sd.demo.network

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.ActivityMainBinding
import com.sd.lib.network.FNetworkObserver

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btn.setOnClickListener {
            logMsg { "isNetworkAvailable:${FNetworkObserver.isNetworkAvailable()} " }
        }

        // 注册观察者
        _networkObserver.register()
    }

    private val _networkObserver = object : FNetworkObserver() {
        override fun onAvailable() {
            logMsg { "onAvailable" }
        }

        override fun onLost() {
            logMsg { "onLost" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消注册
        _networkObserver.unregister()
    }
}

inline fun logMsg(block: () -> Any?) {
    Log.i("network-demo", block().toString())
}