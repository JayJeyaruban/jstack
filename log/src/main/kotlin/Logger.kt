package jstack.log

import jstack.di.DiContext
import jstack.di.retrieve
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias PayloadBuilder = MutableMap<String, Any?>

fun interface Logger {
    fun log(level: Level, payload: PayloadBuilder.() -> Unit)
}

class LoggerLoader internal constructor(private val lf: LoggerFactory) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, Logger> {
        val logger = lf.logger(CallSite.of())
        return ReadOnlyProperty { _, _ -> logger }
    }
}

fun DiContext.logger() = LoggerLoader(retrieve(LoggerFactory))

fun Logger.info(f: PayloadBuilder.() -> Unit) = log(Level.INFO, f)

fun Logger.error(f: PayloadBuilder.() -> Unit) = log(Level.ERROR, f)

fun Logger.debug(f: PayloadBuilder.() -> Unit) = log(Level.DEBUG, f)

fun Logger.trace(f: PayloadBuilder.() -> Unit) = log(Level.TRACE, f)

enum class Level {
    ERROR,
    INFO,
    DEBUG,
    TRACE,
}
