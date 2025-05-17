package jstack.log

import java.util.concurrent.ConcurrentHashMap

fun interface LoggerFactory {
    fun logger(callSite: CallSite): Logger

    companion object : LoggerFactory {
        private val loggers = ConcurrentHashMap<CallSite, Logger>()

        override fun logger(callSite: CallSite) = loggers.computeIfAbsent(callSite) {
            Logger { level, payload ->
                System.err.println("$level\t${callSite.fullPath}\t${buildMap { payload() }}")
            }
        }
    }
}

interface CallSite {
    val fullPath: String

    companion object {
        fun of(): CallSite {
            val stack = Thread.currentThread().stackTrace
            return StackTraceElementCallSite(stack[3])
        }
    }
}

private class StackTraceElementCallSite(private val element: StackTraceElement) : CallSite {
    val segments: List<String> by lazy {
        buildList {
            element.className.split(".").forEach { add(it) }

            if (element.methodName != "<init>") {
                add(element.methodName)
            }
        }
    }

    override val fullPath by lazy { segments.joinToString(".") }

    override fun equals(other: Any?): Boolean = other?.let {
        if (it is StackTraceElementCallSite) {
            it.element == element
        } else {
            null
        }
    } ?: false

    override fun hashCode() = element.hashCode()
}
