package com.sd.lib.network

import android.content.Context
import androidx.startup.Initializer

internal class LibInitializer : Initializer<Context> {
  override fun create(context: Context): Context {
    FNetwork.init(context)
    return context
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return emptyList()
  }
}