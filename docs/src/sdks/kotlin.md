# Kotlin

TODO

## Installation

TODO

### Frontend SDK

TODO

### Backend SDK

TODO

## Usage

Initialize as early as possible.

```kotlin
val ffs = Ffs("TOKEN")
ffs.initialize()
```

Use it.

```kotlin
ffs.isEnabled("test") // true
```

That's it. That's the common workflow. The next code snippet shows more advanced usage.

```kotlin
// Instantiate FFS.
val ffs = Ffs(
    apiToken = "TOKEN", // Required.
    url = "https://your.own.ffs/v1", // Defaults to "https://ffs.delivery/v1".
    liveUpdates = false, // Defaults to true. If false, data will sync once.
)

// Metadata to be used in evaluation. All optional.
ffs.setRolloutId("Doist") // Defaults random string. Controls staged rollouts.
ffs.setUserId(12345)
ffs.setUserEmail("example@ffs.com")
ffs.setDeviceName("Pixel 6 Pro")
ffs.setDeviceOs("Android 12")
ffs.setDeviceLocale("en_US")
ffs.putNumber("app-launches", 42)
ffs.putBoolean("beta", true)
ffs.putListString("experiments", listOf("dark-theme", "outline-icons"))

// Initialize FFS.
// The callback is optional. It runs once it's done initializing.
ffs.initialize {
    val all = all() // Maps all flag names to their evaluation.
    all.forEach { (name, enabled) ->
        println("$name is $enabled")
    }
    
    // Close the connection.
    shutdown()
}

// Check a flag with a default value.
ffs.isEnabled("unknown", false) // false
```
