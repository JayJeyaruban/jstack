package jstack.log

import jstack.core.Loader

fun interface LoggerFactory {
    fun logger(callSite: CallSite): Logger

    companion object : LoggerFactory, Loader<Any, LoggerFactory> {
        override fun logger(callSite: CallSite) =
            Logger { level, payload ->
                System.err.println("$level\t${callSite.fullPath}\t${buildMap { payload() }}")
            }

        override fun Any.load(): LoggerFactory = LoggerFactory
    }
}

class CallSite private constructor(private val element: StackTraceElement) {
    val segments: List<String> by lazy {
        buildList {
            element.className.split(".").forEach { add(it) }

            if (element.methodName != "<init>") {
                add(element.methodName)
            }
        }
    }

    val fullPath by lazy { segments.joinToString(".") }

    companion object {
        fun of(): CallSite {
            val stack = Thread.currentThread().stackTrace
            return CallSite(stack[3])
        }
    }
}
