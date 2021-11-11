package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltak
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Component

@Component
open class TiltakProcessor(
	repository: ArenaDataRepository,
	private val tiltakService: TiltakService
) : AbstractArenaProcessor(repository) {

	override fun insert(data: ArenaData) {
		insertUpdate(data)
	}

	override fun update(data: ArenaData) {
		insertUpdate(data)
	}

	override fun delete(data: ArenaData) {
		throw UnsupportedOperationException("Cannot delete Arena Data for table ${data.tableName}")
	}


	private fun insertUpdate(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltak::class.java)

		if (isSupportedTiltak(newFields.TILTAKSKODE)) {
			tiltakService.upsertTiltak(
				newFields.TILTAKSKODE,
				newFields.TILTAKSNAVN,
				newFields.TILTAKSKODE
			)
		}
	}
}
