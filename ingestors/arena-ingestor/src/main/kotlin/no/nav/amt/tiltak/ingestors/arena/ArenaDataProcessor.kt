package no.nav.amt.tiltak.ingestors.arena

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.processors.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena.processors.TiltakProcessor
import no.nav.amt.tiltak.ingestors.arena.processors.TiltaksgjennomforingProcessor
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class ArenaDataProcessor(
	private val repository: ArenaDataRepository,
	private val tiltakProcessor: TiltakProcessor,
	private val tiltaksgjennomforingProcessor: TiltaksgjennomforingProcessor,
	private val deltakerProcessor: DeltakerProcessor
) {

	val tiltakTableName = "ARENA_GOLDENGATE.TILTAK"
	val tiltakgjennomforingTableName = "ARENA_GOLDENGATE.TILTAKGJENNOMFORING"
	val tiltakDeltakerTableName = "ARENA_GOLDENGATE.TILTAKDELTAKER"

	private val logger = LoggerFactory.getLogger(javaClass)

	fun processUningestedMessages() {
		processMessages { offset -> repository.getUningestedData(tableName = tiltakTableName, offset) }
		processMessages { offset -> repository.getUningestedData(tableName = tiltakgjennomforingTableName, offset) }
		processMessages { offset -> repository.getUningestedData(tableName = tiltakDeltakerTableName, offset) }
	}

	fun processFailedMessages() {
		processMessages { offset -> repository.getFailedData(tableName = tiltakTableName, offset) }
		processMessages { offset -> repository.getFailedData(tableName = tiltakgjennomforingTableName, offset) }
		processMessages { offset -> repository.getFailedData(tableName = tiltakDeltakerTableName, offset) }
	}

	private fun processMessages(getter: (offset: Int) -> List<ArenaData>) {
		var messages: List<ArenaData>
		var offset = 0

		do {
			messages = getter(offset)
			messages.forEach { processMessage(it) }
			offset += messages.size
		} while (messages.isNotEmpty())
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
}
