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
import com.sd.lib.ctx.fContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/** 网络是否可用 */
val fIsNetworkAvailable: Boolean
    get() = libIsNetworkAvailable(fContext)

/** 网络是否可用 */
val fIsNetworkAvailableFlow: Flow<Boolean>
    get() = NetworkObserverHolder.isNetworkAvailable

private object NetworkObserverHolder {
    private val _observer = networkObserver { _isNetworkAvailable.value = it }
    private val _isNetworkAvailable = MutableStateFlow(false)

    val isNetworkAvailable: Flow<Boolean> = _isNetworkAvailable.asStateFlow().drop(1)

    init {
        MainScope().launch {
            _isNetworkAvailable.subscriptionCount
                .map { it > 0 }
                .collect { hasCollector ->
                    if (hasCollector) {
                        _observer.register(fContext).also {
                            _isNetworkAvailable.value = it
                        }
                    } else {
                        _observer.unregister(fContext)
                    }
                }
        }
    }
}

private fun networkObserver(
    onChange: (isAvailable: Boolean) -> Unit,
): NetworkObserver {
    return if (Build.VERSION.SDK_INT >= 24) {
        NewObserver(onChange)
    } else {
        OldObserver(onChange)
    }
}

private abstract class NetworkObserver(
    private val onChange: (isAvailable: Boolean) -> Unit,
) {
    private val _register = AtomicBoolean(false)
    private val _isAvailable = AtomicBoolean(false)

    /**
     * 注册观察者，返回网络是否可用
     */
    fun register(context: Context): Boolean {
        if (_register.compareAndSet(false, true)) {
            _isAvailable.set(libIsNetworkAvailable(context))
            registerImpl(context)
        }
        return _isAvailable.get()
    }

    /**
     * 取消注册观察者
     */
    fun unregister(context: Context) {
        if (_register.compareAndSet(true, false)) {
            unregisterImpl(context)
            _isAvailable.set(false)
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

    protected abstract fun registerImpl(context: Context)

    protected abstract fun unregisterImpl(context: Context)
}

private class NewObserver(
    onChange: (isAvailable: Boolean) -> Unit,
) : NetworkObserver(onChange) {
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

    override fun registerImpl(context: Context) {
        if (Build.VERSION.SDK_INT >= 24) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            manager.registerDefaultNetworkCallback(_observer)
        }
    }

    override fun unregisterImpl(context: Context) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(_observer)
    }
}

private class OldObserver(
    onChange: (isAvailable: Boolean) -> Unit,
) : NetworkObserver(onChange) {
    private val _observer = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                notifyNetworkAvailable(libIsNetworkAvailable(context))
            }
        }
    }

    override fun registerImpl(context: Context) {
        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        context.registerReceiver(_observer, filter)
    }

    override fun unregisterImpl(context: Context) {
        context.unregisterReceiver(_observer)
    }
}

private fun libIsNetworkAvailable(context: Context): Boolean {
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