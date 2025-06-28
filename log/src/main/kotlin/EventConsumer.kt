package jstack.log

import java.io.PrintStream

fun interface EventConsumer {
    fun submit(event: Event)

    companion object {
        inline fun printStream(dst: PrintStream, crossinline transform: (Event) -> String) = EventConsumer { event ->
            dst.println(transform(event))
        }

        inline fun stderr(crossinline transform: (Event) -> String = ::stdEventLineFormat) = printStream(System.err, transform)
    }
}

inline fun stdEventLineFormat(
    event: Event,
    payloadFormatter: (Payload) -> String = { payload ->
        payload.map { (key, value) ->
            "$key=${(value as? Throwable)?.stackTraceToString() ?: value.toString()}"
        }.joinToString("\t")
    },
) = "${event.level}\t${event.callSite.fullPath}\t${payloadFormatter(event.payload)}"

fun EventConsumer.info(payload: PayloadBuilder) = event(Level.INFO, payload)

fun EventConsumer.error(payload: PayloadBuilder) = event(Level.ERROR, payload)

fun EventConsumer.debug(payload: PayloadBuilder) = event(Level.DEBUG, payload)

fun EventConsumer.trace(payload: PayloadBuilder) = event(Level.TRACE, payload)

fun EventConsumer.warn(payload: PayloadBuilder) = event(Level.WARN, payload)

fun EventConsumer.submit(level: Level, payload: PayloadBuilder) = event(level, payload)

private fun EventConsumer.event(level: Level, payload: PayloadBuilder) = submit(
    Event(
        CallSite.walkBack(3),
        level,
        payload,
    ),
)
