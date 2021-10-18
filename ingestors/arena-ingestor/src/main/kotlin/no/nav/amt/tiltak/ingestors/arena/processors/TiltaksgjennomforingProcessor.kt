package no.nav.amt.tiltak.ingestors.arena.processors

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.Tiltaksleverandor
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Component

@Component
class TiltaksgjennomforingProcessor(
	repository: ArenaDataRepository,
	val tiltaksleverandor: Tiltaksleverandor,
	val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	override fun insert(data: ArenaData) {
		val newFields = jsonObject(data.after)
		val virksomhetsnummer = ords.hentVirksomhetsnummer(newFields.ARBGIV_ID_ARRANGOR.toString())

		tiltaksleverandor.addVirksomhet(virksomhetsnummer)
	}

	override fun update(data: ArenaData) {
		TODO("Not yet implemented")
	}

	override fun delete(data: ArenaData) {
		TODO("Not yet implemented")
	}


	// TODO generaliser
	private fun jsonObject(s: String?): ArenaTiltaksgjennomforing {
		if (s == null) {
			throw UnsupportedOperationException("Expected AreanaTiltaksgjennomforing not to be null!")
		}

		return ObjectMapper().readValue(s, ArenaTiltaksgjennomforing::class.java)
	}
}
