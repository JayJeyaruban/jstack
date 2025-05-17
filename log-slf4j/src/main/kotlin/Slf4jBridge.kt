package jstack.log.slf4j

import jstack.log.CallSite
import jstack.log.Logger
import jstack.log.error
import jstack.log.message
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
import jstack.log.LoggerFactory as JLoggerFactory

private val LOGGER_ADAPTER_NAME = LoggerAdapter::class.qualifiedName

class LoggerAdapter(private val inner: Logger, private val level: Level) : LegacyAbstractLogger() {
    override fun isTraceEnabled() = isLevelEnabled(Level.TRACE)

    override fun isDebugEnabled() = isLevelEnabled(Level.DEBUG)

    override fun isInfoEnabled() = isLevelEnabled(Level.TRACE)

    override fun isWarnEnabled() = isLevelEnabled(Level.WARN)

    override fun isErrorEnabled() = isLevelEnabled(Level.ERROR)

    override fun getFullyQualifiedCallerName() = LOGGER_ADAPTER_NAME

    override fun handleNormalizedLoggingCall(
        level: Level,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any?>?,
        error: Throwable?,
    ) {
        val message = MessageFormatter.basicArrayFormat(messagePattern, arguments)
        inner.log(level.intoLevel()) {
            message(message)
            error?.let { error(error) }
        }
    }

    private fun isLevelEnabled(other: Level) = level.toInt() >= other.toInt()
}

object LoggerFactory : ILoggerFactory {
    override fun getLogger(name: String): org.slf4j.Logger {
        val callSite = FqcnCallSite(name)
        return LoggerAdapter(JLoggerFactory.logger(callSite), Level.TRACE)
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

private data class FqcnCallSite(override val fullPath: String) : CallSite

private fun Level.intoLevel(): JLevel {
    return when (this) {
        Level.ERROR -> JLevel.ERROR
        Level.WARN -> JLevel.WARN
        Level.INFO -> JLevel.INFO
        Level.DEBUG -> JLevel.DEBUG
        Level.TRACE -> JLevel.TRACE
    }
}
