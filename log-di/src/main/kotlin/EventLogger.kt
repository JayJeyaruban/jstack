package jstack.log.di

import jstack.core.Loader
import jstack.log.EventFilter
import jstack.log.EventLogger
import jstack.log.pipe

val EventLogger = Loader<Any, EventLogger> {
    EventFilter.env().pipe(EventLogger.stderr())
}
