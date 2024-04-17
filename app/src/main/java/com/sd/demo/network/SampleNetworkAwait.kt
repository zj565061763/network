package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.sd.demo.network.theme.AppTheme
import com.sd.lib.network.fNetworkAwait
import kotlinx.coroutines.launch
import java.util.UUID

class SampleNetworkAwait : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ContentView(
                    onClickLaunch = {
                        lifecycleScope.launch {
                            launchNetWorkAwait()
                        }
                    }
                )
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

@Composable
private fun ContentView(
    modifier: Modifier = Modifier,
    onClickLaunch: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = onClickLaunch) {
            Text(text = "launch")
        }
    }
}