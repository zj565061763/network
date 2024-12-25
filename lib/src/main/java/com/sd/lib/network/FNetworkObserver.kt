package com.sd.lib.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 监听当前网络
 */
abstract class FNetworkObserver {
  private val _scope = MainScope()
  private var _job: Job? = null

  /**
   * 注册
   */
  @Synchronized
  fun register() {
    if (_job == null) {
      _job = _scope.launch {
        FNetwork.currentNetworkFlow.collect {
          onChange(it)
        }
      }
    }
  }

  /**
   * 取消注册
   */
  @Synchronized
  fun unregister() {
    _job?.cancel()
    _job = null
  }

  /**
   * 当前网络状态变化(MainThread)
   */
  protected abstract fun onChange(networkState: NetworkState)
}