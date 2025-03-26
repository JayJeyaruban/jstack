package jstack.log

fun interface LoggerFactory {
    fun logger(callSite: CallSite): Logger

    companion object : LoggerFactory {
        override fun logger(callSite: CallSite) =
            Logger { level, payload ->
                System.err.println("$level\t${callSite.fullPath}\t${buildMap { payload() }}")
            }
    }
}

fun interface LogContext {
    fun loggerFactory(): LoggerFactory

    companion object {
        operator fun invoke() = LogContext { LoggerFactory }
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
