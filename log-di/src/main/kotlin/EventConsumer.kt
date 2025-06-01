package jstack.log.di

import jstack.core.Loader
import jstack.log.EventConsumer
import jstack.log.EventFilter
import jstack.log.pipe

val EventConsumer = Loader<Any, EventConsumer> {
    EventFilter.env().pipe(EventConsumer.stderr())
}
