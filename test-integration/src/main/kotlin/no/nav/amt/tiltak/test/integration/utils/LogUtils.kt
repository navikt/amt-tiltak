package no.nav.amt.tiltak.test.integration.utils

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

object LogUtils {
	private val rootLog: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger

	fun withLogs(fn: (getLogs: () -> List<ILoggingEvent>) -> Unit) {

		val listAppender: ListAppender<ILoggingEvent> = ListAppender<ILoggingEvent>()

		try {
			listAppender.start()
			rootLog.addAppender(listAppender)

			fn { listAppender.list }

		} finally {
			listAppender.stop()
			rootLog.detachAppender(listAppender)
		}
	}
}
