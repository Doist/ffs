# FFS backend library

Library that runs in safe environments, such as a backend. It synchronizes rules with the server and evaluates locally.

The workflow can be as simple as:

1. Initialize FFS.
2. Check if flags are enabled or not.

You can also:

- Connect to a different URL (e.g., your own server instance).
- Enable or disable live updates.
- Run a callback when FFS finishes initializing with fresh data.
- Grab all the data at once (read-only).
- Shutdown explicitly, to free up resources, or stop live updates.

## Usage

Typically, you'll initialize as early as possible:

```kotlin
val ffs = Ffs("TOKEN_BACKEND")
ffs.initialize()
```

And use whenever you want:

```kotlin
ffs.isEnabled("test") // true

// Or, with a default value:
ffs.isEnabled("unknown", false) // false
```

That's it. That's the common workflow. The next code snippet shows more advanced usage.

```kotlin
val ffs = Ffs(
    apiToken = "TOKEN_BACKEND", // Required.
    url = "https://your.own.ffs/v1/", // Defaults to "https://ffs.doist.com".
    liveUpdates = false, // Defaults to true. If false, data will sync once.
)

ffs.initialize {
    val all = all() // Maps all flag names to their evaluations.
    all.forEach { (name, flag) ->
        println("$name is enabled: ${ffs.isEnabled(name)}, archived at: ${flag.archived_at}")
    }
    
    // Close the connection.
    shutdown()
}
```

The code above is Kotlin, but all features are available in all clients.

### JavaScript

```javascript
const ffs = new Ffs("TOKEN_BACKEND")
ffs.initialize()

// ...

ffs.isEnabled("test") // true
ffs.isEnabled("unknown", false) // false
```

### Kotlin

[See Usage.](#usage)

### Swift

```swift
let ffs = Ffs("TOKEN_BACKEND")
ffs.initialize()

// ...

ffs.isEnabled("test") // true
ffs.isEnabled("unknown", false) // false
```

### Python

TBD.

### Ruby

TBD.

### Rust

TBD.

### Go

TBD.

### Java

TBD.

## Installation

TBD.
