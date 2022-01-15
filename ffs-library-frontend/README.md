# FFS frontend library

Library that runs in unsafe environments, such as clients. It fetches rule evaluations from the server.

The workflow can be as simple as:

1. Initialize FFS.
2. Check if flags are enabled or not.

You can also:

- Specify metadata to be used in rule evaluation.
- Connect to a different URL (e.g., your own server instance).
- Enable or disable live updates.
- Run a callback when FFS finishes initializing with fresh data.
- Grab all the data at once (read-only).
- Shutdown explicitly, to free up resources, or stop live updates.

## Usage

Initialize as early as possible.

```kotlin
val ffs = Ffs("TOKEN_FRONTEND")
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
    apiToken = "TOKEN_FRONTEND", // Required.
    url = "https://your.own.ffs/v1/", // Defaults to "https://ffs.doist.com".
    liveUpdates = false, // Defaults to true. If false, data will sync once.
)

// Metadata to be used in evaluation. All optional.
ffs.setRolloutId("Doist") // Defaults to a random string. Controls staged rollouts.
ffs.setUserId(12345)
ffs.setUserEmail("example@ffs.com")
ffs.setDeviceName("Pixel 6 Pro")
ffs.setDeviceLocale("en_US")
ffs.putNumber("app-launches", 42)
ffs.putBoolean("beta", true)
ffs.putListString("experiments", listOf("dark-theme", "outline-icons"))

// Initialize FFS. The callback is optional. It runs once it finishes initializing.
ffs.initialize {
    val all = all() // Maps all flag names to their evaluations.
    all.forEach { (name, flag) ->
        println("$name is enabled: ${ffs.isEnabled(name)}, archived at: ${flag.archived_at}")
    }

    // Close the connection.
    shutdown()
}

// Check a flag with a default value.
ffs.isEnabled("unknown", false) // false
```

The code above is Kotlin, but all features are available in all clients.

### JavaScript

```javascript
const ffs = new Ffs("TOKEN_FRONTEND")
ffs.initialize()

// ...

ffs.isEnabled("test") // true
ffs.isEnabled("unknown", false) // false
```

### Kotlin

[See Usage.](#usage)

### Swift

```swift
let ffs = Ffs("TOKEN_FRONTEND")
ffs.initialize()

// ...

ffs.isEnabled("test") // true
ffs.isEnabled("unknown", false) // false
```

## Installation

TBD.
