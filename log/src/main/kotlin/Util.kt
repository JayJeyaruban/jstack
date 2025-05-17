package jstack.log

fun PayloadBuilder.message(message: String) = put("message", message)

fun PayloadBuilder.error(error: Throwable) = put("error", error)
