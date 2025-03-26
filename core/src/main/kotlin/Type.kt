package jstack.core

import kotlin.reflect.KType
import kotlin.reflect.typeOf

class Type<T>(val t: KType, private val klass: Class<T>) {
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
