[![](https://jitpack.io/v/zj565061763/network.svg)](https://jitpack.io/#zj565061763/network)

# 关于

这是一个获取和监听网络状态的库。

# 常用方法

默认在主进程自动初始化，如果要在其他进程使用，需要在其他进程手动初始化。

```kotlin
// 非主进程，手动初始化
FNetwork.init(context)
```

```kotlin
// 获取当前网络状态
val currentNetwork: NetworkState = FNetwork.getCurrentNetwork()
```

```kotlin
interface NetworkState {
  /** 网络Id */
  val id: String

  /** 是否Wifi网络 */
  val isWifi: Boolean

  /** 是否手机网络 */
  val isCellular: Boolean

  /** 网络是否已连接 */
  val isConnected: Boolean
}
```

# 监听当前网络状态

```kotlin
lifecycleScope.launch {
  FNetwork.currentNetworkFlow.collect { networkState: NetworkState ->
    // 网络状态变化
  }
}
```

# 监听所有网络

```kotlin
lifecycleScope.launch {
  FNetwork.allNetworksFlow.collect { list: List<NetworkState> ->
    // 所有网络状态变化
  }
}
```

# 协程挂起

`fAwaitNetwork`函数：

```kotlin
/**
 * 如果当前网络不满足[condition]，则挂起直到满足[condition]，默认[condition]为网络已连接
 * @return true-调用时已经满足[condition]；false-调用时还不满足[condition]，挂起等待之后满足[condition]
 */
suspend fun fAwaitNetwork(
  condition: (NetworkState) -> Boolean = { it.isConnected },
): Boolean
```