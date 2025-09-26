[![Maven Central](https://img.shields.io/maven-central/v/io.github.zj565061763.android/network)](https://central.sonatype.com/search?q=g:io.github.zj565061763.android+network)

# Gradle

```kotlin
implementation("io.github.zj565061763.android:network:$version")
```

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
val networkState: NetworkState = FNetwork.currentNetworkState()
```

```kotlin
interface NetworkState {
  /** 网络Id */
  val id: String

  /** 是否Wifi网络 */
  val isWifi: Boolean

  /** 是否手机网络 */
  val isCellular: Boolean

  /** 是否有线网络 */
  val isEthernet: Boolean

  /** 网络是否已连接 */
  val isConnected: Boolean
}
```

# 监听当前网络状态

```kotlin
lifecycleScope.launch {
  FNetwork.networkFlow
    // 如果无网络则debounce
    .debounceNoneNetwork()
    .collect { networkState ->
      // 监听当前网络状态
    }
}
```

# 监听所有网络状态

```kotlin
lifecycleScope.launch {
  FNetwork.networksFlow.collect { list: List<NetworkState> ->
    // 监听所有网络状态
  }
}
```

# 协程挂起

```kotlin
/** 如果当前网络未连接，则挂起直到网络连接 */
suspend fun awaitNetworkConnected()

/** 如果当前网络不满足[condition]，则挂起直到满足[condition] */
suspend fun awaitNetwork(condition: (NetworkState) -> Boolean)
```