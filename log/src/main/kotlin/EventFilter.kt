package jstack.log

fun interface EventFilter {
    fun process(event: Event): Event?

    companion object {
        fun env() = ConfigurationFilter(Configuration.fromEnv())
    }
}

fun EventFilter.pipe(consumer: EventLogger) = EventLogger { event ->
    process(event)?.let { consumer.submit(it) }
}

class ConfigurationFilter(private val configuration: Configuration) : EventFilter {
    override fun process(event: Event): Event? = event.takeIf {
        configuration.logLevel(it.callSite) >= event.level
    }
}

@JvmInline
value class DefaultAttributesFilter private constructor(private val attributes: Map<String, PayloadValue.EagerValue<Any>>) : EventFilter {
    constructor(vararg attributes: Pair<String, Any>) :
        this(attributes.associate { (key, value) -> key to PayloadValue.EagerValue(value) })

    override fun process(event: Event) = event.copy(
        payload = buildMap {
            putAll(attributes)
            putAll(event.payload)
        },
    )
}

fun composeFilters(vararg filters: EventFilter) = EventFilter {
    var event: Event? = it
    var i = 0
    while (event != null && i < filters.size) {
        event = filters[i].process(event)
        i += 1
    }
    event
}
