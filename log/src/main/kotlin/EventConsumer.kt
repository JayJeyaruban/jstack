package jstack.log

import java.io.PrintStream
import kotlin.collections.joinToString

fun interface EventConsumer {
    fun submit(event: Event)

    companion object {
        inline fun printStream(dst: PrintStream, crossinline transform: (Event) -> String) = EventConsumer { event ->
            dst.println(transform(event))
        }

        inline fun stderr(crossinline transform: (Event) -> String = ::stdEventLineFormat) = printStream(System.err, transform)
    }
}

inline fun stdEventLineFormat(event: Event, payloadFormatter: (payload: Payload) -> String = ::defaultPayloadFormatter) =
    "${event.level}\t${event.callSite.fullPath}\t${payloadFormatter(event.payload)}"

fun defaultPayloadFormatter(payload: Payload): String = payload.entries.joinToString("\t") { (key, value) ->
    val v = value.value
    "$key=${(v as? Throwable)?.stackTraceToString() ?: v.toString()}"
}

fun EventConsumer.info(payload: PayloadBuilder) = event(Level.INFO, payload)

fun EventConsumer.error(payload: PayloadBuilder) = event(Level.ERROR, payload)

fun EventConsumer.debug(payload: PayloadBuilder) = event(Level.DEBUG, payload)

fun EventConsumer.trace(payload: PayloadBuilder) = event(Level.TRACE, payload)

fun EventConsumer.warn(payload: PayloadBuilder) = event(Level.WARN, payload)

private fun EventConsumer.event(level: Level, payload: PayloadBuilder) = submit(
    Event(
        CallSite.walkBack(3),
        level,
        MutableMapPayloadScope().apply { payload() }.map.toMap(),
    ),
)

@JvmInline
value class MutableMapPayloadScope(val map: MutableMap<String, PayloadValue<*>> = HashMap()) : PayloadBuilderScope {
    override fun <T> set(key: String, value: PayloadValue<T>) {
        map.put(key, value)
    }
}
