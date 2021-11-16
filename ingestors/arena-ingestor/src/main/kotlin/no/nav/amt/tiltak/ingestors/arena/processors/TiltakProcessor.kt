package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltak
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class TiltakProcessor(
	repository: ArenaDataRepository,
	private val tiltakService: TiltakService
) : AbstractArenaProcessor(repository) {

	private val logger = LoggerFactory.getLogger(javaClass)

	override fun insert(data: ArenaData) {
		upsert(data)
	}

	override fun update(data: ArenaData) {
		upsert(data)
	}

	override fun delete(data: ArenaData) {
		logger.error("Delete is not implemented for TiltakProcessor")
		repository.upsert(data.markAsFailed())
	}


	private fun upsert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltak::class.java)

		if (isSupportedTiltak(newFields.TILTAKSKODE)) {
			tiltakService.upsertTiltak(
				newFields.TILTAKSKODE,
				newFields.TILTAKSNAVN,
				newFields.TILTAKSKODE
			)

			repository.upsert(data.markAsIngested())
		} else {
			repository.upsert(data.markAsIgnored())
		}
	}
}
