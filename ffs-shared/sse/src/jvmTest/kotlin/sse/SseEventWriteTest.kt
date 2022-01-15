package doist.ffs.sse

import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertTrue

class SseEventWriteTest {
    @Test
    fun sseEventWriteEndsWithBlankLine() {
        val result = StringWriter().let {
            SseEvent(data = "test").write(it)
            it.toString()
        }
        assertTrue(result.endsWith("\n\n"))
    }

    @Test
    fun sseEventWriteIncludesData() {
        val result = StringWriter().let {
            SseEvent(data = "l1\nl2").write(it)
            it.toString()
        }
        assertTrue(result.contains("${SSE_FIELD_PREFIX_DATA}l1\n${SSE_FIELD_PREFIX_DATA}l2\n"))
    }

    @Test
    fun sseEventWriteIncludesId() {
        val result = StringWriter().let {
            SseEvent(data = "test", id = "id").write(it)
            it.toString()
        }
        assertTrue(result.contains("${SSE_FIELD_PREFIX_ID}id\n"))
    }

    @Test
    fun sseEventWriteIncludesEvent() {
        val result = StringWriter().let {
            SseEvent(data = "test", event = "event").write(it)
            it.toString()
        }
        assertTrue(result.contains("${SSE_FIELD_PREFIX_EVENT}event\n"))
    }
}
