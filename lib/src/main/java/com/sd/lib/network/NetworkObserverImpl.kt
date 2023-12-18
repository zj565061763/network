package com.sd.lib.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

internal fun networkObserver(
    context: Context,
    onAvailable: () -> Unit,
    onLost: () -> Unit,
): NetworkObserver {
    return if (Build.VERSION.SDK_INT >= 24) {
        NewObserver(
            context = context,
            onAvailable = onAvailable,
            onLost = onLost,
        )
    } else {
        OldObserver(
            context = context,
            onAvailable = onAvailable,
            onLost = onLost,
        )
    }
}

internal abstract class NetworkObserver(
    protected val context: Context,
    private val onAvailable: () -> Unit,
    private val onLost: () -> Unit,
) {
    private val _register = AtomicBoolean()
    private var _isNetworkAvailable: Boolean? = null

    fun register(): Boolean {
        if (_register.compareAndSet(false, true)) {
            _isNetworkAvailable = libIsNetworkAvailable(context)
            registerImpl()
            return true
        }
        return false
    }

    fun unregister() {
        if (_register.compareAndSet(true, false)) {
            unregisterImpl()
            _isNetworkAvailable = null
        }
    }

    fun notifyCallback() {
        if (!_register.get()) return
        Handler(Looper.getMainLooper()).post {
            if (_register.get()) {
                _isNetworkAvailable?.let { isAvailable ->
                    if (isAvailable) onAvailable() else onLost()
                }
            }
        }
    }

    protected fun notifyNetworkAvailable(isAvailable: Boolean) {
        if (!_register.get()) return
        if (_isNetworkAvailable != isAvailable) {
            _isNetworkAvailable = isAvailable
            notifyCallback()
        }
    }

    protected abstract fun registerImpl()

    protected abstract fun unregisterImpl()
}

private class NewObserver(
    context: Context,
    onAvailable: () -> Unit,
    onLost: () -> Unit,
) : NetworkObserver(
    context = context,
    onAvailable = onAvailable,
    onLost = onLost,
) {
    private val _observer = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            notifyNetworkAvailable(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            notifyNetworkAvailable(false)
        }
    }

    override fun registerImpl() {
        if (Build.VERSION.SDK_INT >= 24) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            manager.registerDefaultNetworkCallback(_observer)
        }
    }

    override fun unregisterImpl() {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(_observer)
    }
}

private class OldObserver(
    context: Context,
    onAvailable: () -> Unit,
    onLost: () -> Unit,
) : NetworkObserver(
    context = context,
    onAvailable = onAvailable,
    onLost = onLost,
) {
    private val _observer = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                notifyNetworkAvailable(libIsNetworkAvailable(context))
            }
        }
    }

    override fun registerImpl() {
        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        context.registerReceiver(_observer, filter)
    }

    override fun unregisterImpl() {
        context.unregisterReceiver(_observer)
    }
}

internal fun libIsNetworkAvailable(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= 23) {
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        return manager.activeNetworkInfo?.isConnected ?: return false
    }
}