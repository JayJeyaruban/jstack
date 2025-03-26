package jstack.rpc.jdk

import jstack.core.Loader
import jstack.core.Type
import jstack.di.DiContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

val Executor = Loader<Any, Executor> { Executors.newVirtualThreadPerTaskExecutor() }

class ConcurrentMapDiContext() : DiContext {
    private val storage = ConcurrentHashMap<Type<*>, Any?>()
    private val path = mutableSetOf<Type<*>>()

    override fun <T> getOrDefault(t: Type<T>, f: () -> T): T = t.cast(
        storage.computeIfAbsent(t) {
            if (t in path) {
                error("Cyclic dependency detected: $path")
            }
            path.add(t)

            f().also { path.remove(t) }
        },
    )
}
