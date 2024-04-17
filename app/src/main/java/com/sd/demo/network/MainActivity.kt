package com.sd.demo.network

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.ActivityMainBinding
import com.sd.lib.network.FNetwork

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        // 初始化
        FNetwork.init(this)

        _binding.btnSampleCurrentNetwork.setOnClickListener {
            startActivity(Intent(this, SampleCurrentNetwork::class.java))
        }
        _binding.btnSampleAllNetworks.setOnClickListener {
            startActivity(Intent(this, SampleAllNetworks::class.java))
        }
        _binding.btnSampleNetworkAwait.setOnClickListener {
            startActivity(Intent(this, SampleNetworkAwait::class.java))
        }
    }
}

inline fun logMsg(block: () -> Any?) {
    Log.i("network-demo", block().toString())
}