package jstack.log

import jstack.di.DiContext
import kotlin.test.Test

class LoggerTest {
    @Test
    fun test() = with(DiContext()) {
        sampleFn()
        val sample = SampleClass(this)
        println("Created sample")
        sample.sample()
        sample.otherMethod()
    }
}

fun DiContext.sampleFn() {
    val log by logger()
    log.info {
        put("hello", "world")
    }
}

class SampleClass(ctx: DiContext) : DiContext by ctx {
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
