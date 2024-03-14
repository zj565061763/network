# Gradle

[![](https://jitpack.io/v/zj565061763/network.svg)](https://jitpack.io/#zj565061763/network)

# Sample

```kotlin
val isNetworkAvailable = FNetworkObserver.isNetworkAvailable()
```

```kotlin
private val networkObserver = object : FNetworkObserver() {
    override fun onChange(isAvailable: Boolean) {
        // network state changed
    }
}

// register
networkObserver.register()

// unregister
networkObserver.unregister()
```

```kotlin
private suspend fun launchNetWorkAvailable() {
    // Suspend until the network is available.
    fAwaitNetworkAvailable()
}
```