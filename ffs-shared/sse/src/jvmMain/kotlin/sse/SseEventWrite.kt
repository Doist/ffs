package doist.ffs.sse

import java.io.Writer

fun SseEvent.write(writer: Writer) {
    if (id != null) {
        writer.write("$SSE_FIELD_PREFIX_ID$id\n")
    }
    if (event != null) {
        writer.write("$SSE_FIELD_PREFIX_EVENT$event\n")
    }
    for (line in data.lines()) {
        writer.write("$SSE_FIELD_PREFIX_DATA$line\n")
    }
    writer.write("\n")
    writer.flush()
}
