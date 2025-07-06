package jstack.log

data class Event(
    val callSite: CallSite,
    val level: Level,
    val payload: Payload,
) : Payload by payload

typealias Payload = Map<String, PayloadValue<*>>
typealias PayloadBuilder = PayloadBuilderScope.() -> Unit

interface PayloadBuilderScope {
    fun <T> set(key: String, value: PayloadValue<T>)
}

sealed interface PayloadValue<out T> {
    val value: T

    @JvmInline
    value class EagerValue<T>(override val value: T) : PayloadValue<T>

    @JvmInline
    value class LazyValue<T>(val lazy: Lazy<T>) : PayloadValue<T> {
        override val value: T get() {
            val v by lazy
            return v
        }
    }
}
