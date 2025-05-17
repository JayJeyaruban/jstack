package jstack.log.di

import jstack.core.Loader
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.log.CallSite
import jstack.log.Logger
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import jstack.log.LoggerFactory as JStackLoggerFactory

val LoggerFactory = Loader<Any, JStackLoggerFactory> {
    JStackLoggerFactory
}

fun DiContext.logger() = LoggerLoader(retrieve(LoggerFactory))

class LoggerLoader internal constructor(private val lf: JStackLoggerFactory) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, Logger> {
        val logger = lf.logger(CallSite.of())
        return ReadOnlyProperty { _, _ -> logger }
    }
}
