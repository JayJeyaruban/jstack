package jstack.di

import jstack.core.Loader
import jstack.core.Type
import java.util.concurrent.ConcurrentHashMap

interface DiContext {
    fun <T> getOrDefault(t: Type<T>, f: () -> T): T

    companion object {
        operator fun invoke(): DiContext = MapBasedDiContext()
    }
}

inline fun <C : DiContext, reified T> C.retrieve(dep: Loader<C, T>) = getOrDefault(Type.of<T>()) { dep.run { load() } }

fun <C : DiContext> C.register(vararg deps: Loader<C, *>) {
    deps.forEach { retrieve(it) }
}

internal class MapBasedDiContext : DiContext {
    private val storage = mutableMapOf<Type<*>, Any?>()
    private val path = mutableSetOf<Type<*>>()

    override fun <T> getOrDefault(t: Type<T>, f: () -> T): T {
        if (t in path) {
            error("Cyclic dependency detected: $path")
        }
        path.add(t)

        if (t !in storage) {
            storage[t] = f()
        }

        return t.cast(storage[t]).also { path.remove(t) }
    }
}

class ConcurrentMapBasedDiContext : DiContext {
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
