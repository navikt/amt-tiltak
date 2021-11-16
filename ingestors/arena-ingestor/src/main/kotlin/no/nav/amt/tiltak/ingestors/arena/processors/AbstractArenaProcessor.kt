package no.nav.amt.tiltak.ingestors.arena.processors

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory

abstract class AbstractArenaProcessor(
	protected val repository: ArenaDataRepository
) {

	companion object {
		private const val MAX_INGEST_ATTEMPTS = 10

		private val SUPPORTED_TILTAK = setOf(
			"ARBFORB",
			"AVKLARAG",
			"GRUPPEAMO",
			"INDOPPFAG",
			"JOBBK",
			"VASV"
		)

	}

	private val logger = LoggerFactory.getLogger(javaClass)

	fun handle(data: ArenaData) {
		try {
			when (data.operationType) {
				OperationType.INSERT -> {
					insert(data)
				}
				OperationType.UPDATE -> {
					update(data)
				}
				OperationType.DELETE -> {
					delete(data)
				}
			}
		} catch (e: Exception) {
			if (data.ingestAttempts >= MAX_INGEST_ATTEMPTS) {
				logger.error(e.message, e)
				repository.upsert(data.markAsFailed())
			} else {
				if (e !is DependencyNotIngestedException) {
					logger.error(e.message, e)
				}

				repository.upsert(data.retry())
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

		return jacksonObjectMapper().readValue(string, clazz)
	}

	protected fun isSupportedTiltak(tiltakskode: String): Boolean {
		return SUPPORTED_TILTAK.contains(tiltakskode)
	}
}
