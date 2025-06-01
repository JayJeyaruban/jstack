package jstack.log

fun PayloadBuilderScope.message(message: String) = put("message", message)

fun PayloadBuilderScope.error(error: Throwable) = put("error", error)
