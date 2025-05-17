package jstack.log

import kotlin.test.Test

class LoggerTest {
    @Test
    fun test() {
        sampleFn()
        val sample = SampleClass()
        println("Created sample")
        sample.sample()
        sample.otherMethod()
    }
}

fun sampleFn() {
    val log = LoggerFactory.logger(CallSite.of())
    log.info {
        put("hello", "world")
    }
}

class SampleClass() {
    private val log = LoggerFactory.logger(CallSite.of())

    fun sample() {
        log.info { message("Hello world") }
    }

    fun otherMethod() {
        log.info { message("Before") }
        val log = LoggerFactory.logger(CallSite.of())
        log.info { message("After") }
    }
}
