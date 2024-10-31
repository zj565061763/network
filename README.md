[![](https://jitpack.io/v/zj565061763/network.svg)](https://jitpack.io/#zj565061763/network)

# 关于

默认在主进程自动初始化，如果要在其他进程使用，需要在其他进程手动初始化。

```kotlin
// 初始化
FNetwork.init(context)
```

# 常用方法

```kotlin
// 当前网络是否已连接，已连接不代表网络一定可用
FNetwork.currentNetwork.isConnected()

// 当前网络是否可用
FNetwork.currentNetwork.isAvailable()

// 当前网络是否Wifi
FNetwork.currentNetwork.isWifi()

// 当前网络是否手机网络
FNetwork.currentNetwork.isCellular()
```

# 监听当前网络状态

#### 常规监听

```kotlin
private val networkObserver = object : FNetworkObserver() {
   override fun onChange(networkState: NetworkState) {
      // 网络状态变化
   }
}

// 注册监听
networkObserver.register()

// 取消注册监听
networkObserver.unregister()
```

#### Flow监听

```kotlin
lifecycleScope.launch {
   FNetwork.currentNetworkFlow.collect { networkState ->
      // 网络状态变化
   }
}
```

# 监听所有网络

```kotlin
lifecycleScope.launch {
   FNetwork.allNetworksFlow.collect { list ->
      // 所有网络状态变化
   }
}
```

# 协程挂起

```kotlin
lifecycleScope.launch {
   // 默认情况下，如果网络未连接，会挂起直到网络已连接
   fNetwork()
}
```

`fNetwork`函数：

```kotlin
/**
 * 如果满足[condition]，直接返回，否则挂起直到满足[condition]
 */
suspend fun fNetwork(
   condition: (NetworkState) -> Boolean = { it.isConnected() },
)
```