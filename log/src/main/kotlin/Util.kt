package jstack.log

fun PayloadBuilder.message(message: String) = put("message", message)

fun PayloadBuilder.error(error: Any) = put("error", error)
