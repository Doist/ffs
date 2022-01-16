# Server

Serves the API for the SDKs and dashboard.

## Architecture

It uses [Ktor](https://ktor.io/), a coroutines-based library for building web applications in Kotlin. It follows many of its design recommendations, especially around routing.

## Data layer

The data layer is implemented using [SQLDelight](https://cashapp.github.io/sqldelight/), a data storage and access library where the database is the canonical source of instances at runtime, i.e., it generates type-safe Kotlin APIs from SQL. It has built-in support for generating migrations, too.
