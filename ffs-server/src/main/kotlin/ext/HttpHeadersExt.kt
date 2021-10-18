package doist.ffs.ext

import io.ktor.http.HttpHeaders

val HttpHeaders.LastEventID get() = "Last-Event-ID"
