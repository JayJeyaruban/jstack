package jstack.di

import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DiContext {
    fun <T> getOrDefault(
        t: Type<T>,
        f: () -> T,
    ): T
}

inline fun <C : DiContext, reified T> C.retrieve(dep: Loader<C, T>): T {
    val t = Type.of<T>()
    return getOrDefault(t) { dep.run { load() } }
}

inline fun <C : DiContext, reified T> C.register(dep: Loader<C, T>) {
    retrieve(dep)
}

class Type<T>(private val t: KType, private val klass: Class<T>) {
    override fun equals(other: Any?) =
        other?.let { it as? Type<*> }?.let {
            it.t == t
        } ?: false

    override fun hashCode() = t.hashCode()

    fun cast(obj: Any?) = klass.cast(obj)

    companion object {
        inline fun <reified T> of() = Type(typeOf<T>(), T::class.java)
    }
}

class MapBasedDiContext : DiContext {
    private val storage = mutableMapOf<Type<*>, Any?>()
    private val path = mutableSetOf<Type<*>>()

    override fun <T> getOrDefault(
        t: Type<T>,
        f: () -> T,
    ): T {
        if (t in path) {
            error("Cyclic dependency detected")
        }
        path.add(t)

        if (t !in storage) {
            storage[t] = f()
        }

        return t.cast(storage[t]).also { path.remove(t) }
    }
}
