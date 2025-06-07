package jstack.log

data class Event(
    val callSite: CallSite,
    val level: Level,
    private val payloadBuilder: PayloadBuilder,
) {
    val payload by lazy { buildMap { payloadBuilder() } }
}

typealias Payload = Map<String, Any?>
typealias PayloadBuilder = PayloadBuilderScope.() -> Unit
typealias PayloadBuilderScope = MutableMap<String, Any?>
