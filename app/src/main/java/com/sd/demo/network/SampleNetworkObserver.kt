package com.sd.demo.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sd.demo.network.theme.AppTheme
import com.sd.lib.network.FNetworkObserver
import com.sd.lib.network.NetworkState

class SampleNetworkObserver : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         AppTheme {
            ContentView { checked ->
               if (checked) {
                  _observer.register()
               } else {
                  _observer.unregister()
               }
            }
         }
      }
   }

   private val _observer = object : FNetworkObserver() {
      override fun onChange(networkState: NetworkState) {
         networkState.log()
      }
   }

   override fun onDestroy() {
      super.onDestroy()
      _observer.unregister()
   }
}

@Composable
private fun ContentView(
   modifier: Modifier = Modifier,
   onCheckedChange: (Boolean) -> Unit,
) {
   var checked by remember { mutableStateOf(false) }
   Column(
      modifier = modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
   ) {
      Switch(
         checked = checked,
         onCheckedChange = {
            checked = it
            onCheckedChange(it)
         },
      )
   }
}