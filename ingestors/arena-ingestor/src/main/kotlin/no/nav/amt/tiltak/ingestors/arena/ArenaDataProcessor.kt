package no.nav.amt.tiltak.ingestors.arena

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.IngestStatus
import no.nav.amt.tiltak.ingestors.arena.processors.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena.processors.TiltakProcessor
import no.nav.amt.tiltak.ingestors.arena.processors.TiltaksgjennomforingProcessor
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
open class ArenaDataProcessor(
	private val repository: ArenaDataRepository,
	private val tiltakProcessor: TiltakProcessor,
	private val tiltaksgjennomforingProcessor: TiltaksgjennomforingProcessor,
	private val deltakerProcessor: DeltakerProcessor
) {

	val tiltakTableName = "SIAMO.TILTAK"
	val tiltakgjennomforingTableName = "SIAMO.TILTAKGJENNOMFORING"
	val tiltakDeltakerTableName = "SIAMO.TILTAKDELTAKER"

	private val logger = LoggerFactory.getLogger(javaClass)

	fun processUningestedMessages() {
		processMessages { repository.getUningestedData(tableName = tiltakTableName) }
		if (!hasNewTiltak()) processMessages { repository.getUningestedData(tableName = tiltakgjennomforingTableName) }
		if (!hasNewTiltaksgjennomforinger()) processMessages { repository.getUningestedData(tableName = tiltakDeltakerTableName) }
	}

	fun processFailedMessages() {
		processMessages { repository.getFailedData(tableName = tiltakTableName) }
		if (!hasNewTiltak()) processMessages { repository.getFailedData(tableName = tiltakgjennomforingTableName) }
		if (!hasNewTiltaksgjennomforinger()) processMessages { repository.getFailedData(tableName = tiltakDeltakerTableName) }
	}

	private fun processMessages(getter: () -> List<ArenaData>) {
		var messages: List<ArenaData>

		do {
			val start = Instant.now()
			messages = getter()
			messages.forEach { processMessage(it) }
			log(start, messages)
		} while (messages.isNotEmpty())
	}

	private fun log(start: Instant, messages: List<ArenaData>) {
		if (messages.isEmpty()) {
			return
		}
		val table = messages.first().tableName
		val first = messages.first().operationPosition
		val last = messages.last().operationPosition
		val duration = Duration.between(start, Instant.now())

		logger.info("[$table]: Handled from id $first to $last in ${duration.toMillis()} ms. (${messages.size} items)")
	}

	private fun processMessage(data: ArenaData) {
		when (data.tableName.uppercase()) {
			tiltakTableName -> tiltakProcessor.handle(data)
			tiltakgjennomforingTableName -> tiltaksgjennomforingProcessor.handle(data)
			tiltakDeltakerTableName -> deltakerProcessor.handle(data)
			else -> {
				logger.error("Data from table ${data.tableName} if not supported")
				repository.upsert(data.markAsFailed())
			}
		}
	}

	private fun hasNewTiltak(): Boolean {
		return repository.getByIngestStatusIn(tiltakTableName, listOf(IngestStatus.NEW), 0, 1).isNotEmpty()
	}

	private fun hasNewTiltaksgjennomforinger(): Boolean {
		return repository.getByIngestStatusIn(tiltakgjennomforingTableName, listOf(IngestStatus.NEW), 0, 1).isNotEmpty()
	}
}
