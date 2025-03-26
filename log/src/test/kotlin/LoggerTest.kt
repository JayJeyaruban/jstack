package jstack.log

import kotlin.test.Test

class LoggerTest {
    @Test
    fun test() = with(LogContext()) {
        sampleFn()
        val sample = SampleClass(this)
        println("Created sample")
        sample.sample()
        sample.otherMethod()
    }
}

fun LogContext.sampleFn() {
    val log by logger()
    log.info {
        put("hello", "world")
    }
}

class SampleClass(logCtx: LogContext) : LogContext by logCtx {
    private val log by logger()

    fun sample() {
        log.info { message("Hello world") }
    }

    fun otherMethod() {
        log.info { message("Before") }
        val log by logger()
        log.info { message("After") }
    }
}
