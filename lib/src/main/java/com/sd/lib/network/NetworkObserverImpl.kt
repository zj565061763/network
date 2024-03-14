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
import java.util.concurrent.atomic.AtomicReference

internal fun networkObserver(
    context: Context,
    onChange: (isAvailable: Boolean) -> Unit,
): NetworkObserver {
    return if (Build.VERSION.SDK_INT >= 24) {
        NewObserver(
            context = context,
            onChange = onChange,
        )
    } else {
        OldObserver(
            context = context,
            onChange = onChange,
        )
    }
}

internal abstract class NetworkObserver(
    protected val context: Context,
    private val onChange: (isAvailable: Boolean) -> Unit,
) {
    private val _register = AtomicBoolean(false)
    private val _isAvailable = AtomicReference<Boolean?>(null)

    fun register(notify: Boolean) {
        if (_register.compareAndSet(false, true)) {
            val isAvailable = libIsNetworkAvailable(context)
            _isAvailable.set(isAvailable)
            registerImpl()
            if (notify) notifyCallback(isAvailable)
        }
    }

    fun unregister() {
        if (_register.compareAndSet(true, false)) {
            unregisterImpl()
            _isAvailable.set(null)
        }
    }

    protected fun notifyNetworkAvailable(isAvailable: Boolean) {
        if (!_register.get()) return
        val oldValue = _isAvailable.getAndSet(isAvailable)
        if (oldValue != isAvailable) {
            notifyCallback(isAvailable)
        }
    }

    private fun notifyCallback(isAvailable: Boolean) {
        if (!_register.get()) return
        Handler(Looper.getMainLooper()).post {
            if (_register.get()) {
                onChange(isAvailable)
            }
        }
    }

    protected abstract fun registerImpl()

    protected abstract fun unregisterImpl()
}

private class NewObserver(
    context: Context,
    onChange: (isAvailable: Boolean) -> Unit,
) : NetworkObserver(
    context = context,
    onChange = onChange,
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
    onChange: (isAvailable: Boolean) -> Unit,
) : NetworkObserver(
    context = context,
    onChange = onChange,
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
    val network = manager.activeNetwork ?: return false
    val capabilities = manager.getNetworkCapabilities(network) ?: return false
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}