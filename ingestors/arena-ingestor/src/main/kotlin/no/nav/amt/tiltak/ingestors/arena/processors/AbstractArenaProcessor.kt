package no.nav.amt.tiltak.ingestors.arena.processors

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

abstract class AbstractArenaProcessor(
	private val repository: ArenaDataRepository
) {

	companion object {
		private val MAX_INGEST_ATTEMPTS = 10
	}

	private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	private val logger = LoggerFactory.getLogger(javaClass)

	fun handle(data: ArenaData) {
		try {
			when (data.operationType) {
				OperationType.INSERT -> {
					insert(data)
					repository.markAsIngested(data.id)
				}
				OperationType.UPDATE -> {
					update(data)
					repository.markAsIngested(data.id)
				}
				OperationType.DELETE -> {
					delete(data)
					repository.markAsIngested(data.id)
				}
			}
		} catch (e: Exception) {
			if (data.ingestAttempts >= MAX_INGEST_ATTEMPTS) {
				repository.setFailed(data, e.message, e)
			} else {
				repository.setRetry(data)
			}
		}
	}

	protected abstract fun insert(data: ArenaData)
	protected abstract fun update(data: ArenaData)
	protected abstract fun delete(data: ArenaData)

	protected fun <T> jsonObject(string: String?, clazz: Class<T>): T {
		if (string == null) {
			throw UnsupportedOperationException("Expected ${clazz.simpleName} not to be null!")
		}

		return ObjectMapper().readValue(string, clazz)
	}

	protected fun stringToLocalDate(string: String?): LocalDate? {
		return if (string != null) LocalDate.parse(string, formatter) else null
	}

	protected fun stringToLocalDateTime(string: String?): LocalDateTime? {
		return if (string != null) LocalDateTime.parse(string, formatter) else null
	}

	/**
	 * Har ingen eksempler på hvordan klokketid ser ut, så må vente med det til vi har et eksempel.
	 */
	protected fun datoKlokketidToLocalDateTime(dato: String?, klokketid: String?): LocalDateTime? {
		if (dato == null) {
			return null
		}

		val date = LocalDate.parse(dato, formatter)

		val time = if (klokketid != null) {
			logger.warn("Det er ikke implementert en handler for klokketid, pattern: $klokketid")
			LocalTime.MIDNIGHT
		} else LocalTime.MIDNIGHT

		return LocalDateTime.of(date, time)
	}

}
