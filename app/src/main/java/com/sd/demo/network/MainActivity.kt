package com.sd.demo.network

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.ActivityMainBinding
import com.sd.lib.coroutine.FScope
import com.sd.lib.network.FNetworkObserver
import com.sd.lib.network.fAwaitNetworkAvailable
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val _scope = FScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        // 注册观察者
        _networkObserver.register()

        _binding.btnLaunch.setOnClickListener {
            _scope.launch {
                launchNetWorkAvailable()
            }
        }

        _binding.btnCancel.setOnClickListener {
            _scope.cancel()
        }
    }

    private val _networkObserver = object : FNetworkObserver() {
        override fun onAvailable() {
            logMsg { "onAvailable" }
        }

        override fun onLost() {
            logMsg { "onLost" }
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

    override fun onResume() {
        super.onResume()
        logMsg { "isNetworkAvailable:${FNetworkObserver.isNetworkAvailable()} " }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消注册
        _networkObserver.unregister()

        _scope.cancel()
    }
}

inline fun logMsg(block: () -> Any?) {
    Log.i("network-demo", block().toString())
}