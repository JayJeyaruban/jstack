package jstack.log

import kotlin.test.Test
import kotlin.test.assertEquals

class EventLoggerTest {
    @Test
    fun test() {
        sampleFn()
        val sample = SampleClass()
        println("Created sample")
        sample.sample()
        sample.otherMethod()

        assertEquals(
            listOf(
                mapOf(
                    "hello" to "world",
                    "level" to "INFO",
                    "call-site" to "jstack.log.EventLoggerTestKt.sampleFn",
                ),
                mapOf(
                    "message" to "Hello world",
                    "level" to "INFO",
                    "call-site" to "jstack.log.SampleClass.sample",
                ),
                mapOf(
                    "message" to "Before",
                    "level" to "INFO",
                    "call-site" to "jstack.log.SampleClass.otherMethod",
                ),
                mapOf(
                    "message" to "After",
                    "level" to "INFO",
                    "call-site" to "jstack.log.SampleClass.otherMethod",
                ),
            ),
            CentralEventLogger.events.map { it.flatten() },
        )
    }
}

fun sampleFn() {
    val log = CentralEventLogger
    log.info {
        setAttribute("hello", "world")
    }
}

class SampleClass() {
    private val log = CentralEventLogger

    fun sample() {
        log.info { message("Hello world") }
    }

    fun otherMethod() {
        log.info { message("Before") }
        val log = CentralEventLogger
        log.info { message("After") }
    }
}

private object CentralEventLogger : EventLogger {
    val events = ArrayList<Event>()

    override fun submit(event: Event) {
        events.add(event)
    }
}

private fun Event.flatten() = buildMap {
    this@flatten.forEach { (key, value) ->
        put(key, value.value.toString())
    }
    put("level", level.toString())
    put("call-site", callSite.fullPath)
}
