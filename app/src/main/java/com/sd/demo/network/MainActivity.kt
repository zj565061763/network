package com.sd.demo.network

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.network.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnSampleNetworkObserver.setOnClickListener {
            startActivity(Intent(this, SampleNetworkObserver::class.java))
        }
        _binding.btnSampleAwaitNetwork.setOnClickListener {
            startActivity(Intent(this, SampleAwaitNetwork::class.java))
        }
    }
}

inline fun logMsg(block: () -> Any?) {
    Log.i("network-demo", block().toString())
}