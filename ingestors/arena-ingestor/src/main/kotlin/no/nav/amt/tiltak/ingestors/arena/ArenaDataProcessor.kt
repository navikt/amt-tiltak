package no.nav.amt.tiltak.ingestors.arena

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.processors.DeltakerProcessor
import no.nav.amt.tiltak.ingestors.arena.processors.TiltakProcessor
import no.nav.amt.tiltak.ingestors.arena.processors.TiltaksgjennomforingProcessor
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
open class ArenaDataProcessor(
	private val repository: ArenaDataRepository,
	private val tiltakProcessor: TiltakProcessor,
	private val tiltaksgjennomforingProcessor: TiltaksgjennomforingProcessor,
	private val deltakerProcessor: DeltakerProcessor
) {

	fun processUningestedMessages() {
		processMessages { offset -> repository.getUningestedData(offset) }
	}

	fun processFailedMessages() {
		processMessages { offset -> repository.getFailedData(offset) }
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
			"ARENA_GOLDENGATE.TILTAK" -> tiltakProcessor.handle(data)
			"ARENA_GOLDENGATE.TILTAKSGJENNOMFORING" -> tiltaksgjennomforingProcessor.handle(data)
			"ARENA_GOLDENGATE.TILTAKDELTAKER" -> deltakerProcessor.handle(data)
			else -> repository.setFailed(data, "Data from table ${data.tableName} if not supported")
		}
	}
}
