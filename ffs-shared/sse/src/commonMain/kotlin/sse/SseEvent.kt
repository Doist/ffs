package doist.ffs.sse

import io.ktor.http.HttpHeaders

const val SSE_DEFAULT_RETRY = 3000L

const val SSE_FIELD_PREFIX_ID = "id: "
const val SSE_FIELD_PREFIX_EVENT = "event: "
const val SSE_FIELD_PREFIX_DATA = "data: "
const val SSE_FIELD_PREFIX_RETRY = "retry: "

val HttpHeaders.LastEventID get() = "Last-Event-ID"

data class SseEvent(val data: String, val event: String? = null, val id: String? = null)
