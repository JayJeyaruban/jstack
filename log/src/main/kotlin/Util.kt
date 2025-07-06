package jstack.log

fun PayloadBuilderScope.message(message: String) = setAttribute("message", message)

fun PayloadBuilderScope.error(error: Throwable) = setAttribute("error", error)

fun <T> PayloadBuilderScope.setAttribute(key: String, value: T) = set(key, PayloadValue.EagerValue(value))

fun <T> PayloadBuilderScope.lazyAttribute(key: String, mode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE, lazy: () -> T) = set(
    key,
    PayloadValue.LazyValue(lazy(mode, lazy)),
)
