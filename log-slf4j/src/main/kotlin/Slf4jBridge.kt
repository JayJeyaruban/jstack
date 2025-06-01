package jstack.log.slf4j

import jstack.log.CallSite
import jstack.log.Configuration
import jstack.log.ConfigurationFilter
import jstack.log.Event
import jstack.log.EventConsumer
import jstack.log.logLevel
import jstack.log.message
import jstack.log.pipe
import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.helpers.BasicMarkerFactory
import org.slf4j.helpers.LegacyAbstractLogger
import org.slf4j.helpers.MessageFormatter
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider
import jstack.log.Level as JLevel

private val LOGGER_ADAPTER_NAME = EventConsumerLoggerAdapter::class.qualifiedName

class EventConsumerLoggerAdapter(
    private val inner: EventConsumer,
    private val callSite: CallSite,
    private val level: JLevel,
) : LegacyAbstractLogger() {
    override fun isTraceEnabled() = isLevelEnabled(JLevel.TRACE)

    override fun isDebugEnabled() = isLevelEnabled(JLevel.DEBUG)

    override fun isInfoEnabled() = isLevelEnabled(JLevel.TRACE)

    override fun isWarnEnabled() = isLevelEnabled(JLevel.WARN)

    override fun isErrorEnabled() = isLevelEnabled(JLevel.ERROR)

    override fun getFullyQualifiedCallerName() = LOGGER_ADAPTER_NAME

    override fun handleNormalizedLoggingCall(
        level: Level,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any?>?,
        error: Throwable?,
    ) {
        val message = MessageFormatter.basicArrayFormat(messagePattern, arguments)
        inner.submit(
            Event(
                callSite,
                level.intoLevel(),
            ) {
                message(message)
                error?.let { error(error) }
            },
        )
    }

    private fun isLevelEnabled(other: JLevel) = level >= other
}

object LoggerFactory : ILoggerFactory {
    private var configuration: Configuration = Configuration.fromEnv()
    private var sink: EventConsumer = EventConsumer.stderr()

    val consumer by lazy { ConfigurationFilter(configuration).pipe(sink) }

    fun register(configuration: Configuration, sink: EventConsumer) {
        this.configuration = configuration
        this.sink = sink
    }

    override fun getLogger(name: String): org.slf4j.Logger {
        val callSite = FqcnCallSite(name)
        return EventConsumerLoggerAdapter(consumer, callSite, configuration.logLevel(callSite))
    }
}

class JStackLogServiceProvider : SLF4JServiceProvider {
    private val mf = BasicMarkerFactory()
    private val mdcAdapter = BasicMDCAdapter()

    override fun getLoggerFactory(): ILoggerFactory = LoggerFactory

    override fun getMarkerFactory(): IMarkerFactory = mf

    override fun getMDCAdapter(): MDCAdapter = mdcAdapter

    override fun getRequestedApiVersion(): String = "2.0.99"

    override fun initialize() {}
}

private data class FqcnCallSite(val fullPath: String) : CallSite {
    override val path get() = fullPath.split(".")
}

private fun Level.intoLevel(): JLevel {
    return when (this) {
        Level.ERROR -> JLevel.ERROR
        Level.WARN -> JLevel.WARN
        Level.INFO -> JLevel.INFO
        Level.DEBUG -> JLevel.DEBUG
        Level.TRACE -> JLevel.TRACE
    }
}
