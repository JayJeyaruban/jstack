package jstack.log

typealias PayloadBuilder = MutableMap<String, Any?>

fun interface Logger {
    fun log(level: Level, payload: PayloadBuilder.() -> Unit)
}

fun Logger.info(f: PayloadBuilder.() -> Unit) = log(Level.INFO, f)

fun Logger.error(f: PayloadBuilder.() -> Unit) = log(Level.ERROR, f)

fun Logger.debug(f: PayloadBuilder.() -> Unit) = log(Level.DEBUG, f)

fun Logger.trace(f: PayloadBuilder.() -> Unit) = log(Level.TRACE, f)

fun Logger.warn(f: PayloadBuilder.() -> Unit) = log(Level.WARN, f)

enum class Level {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE,
}
