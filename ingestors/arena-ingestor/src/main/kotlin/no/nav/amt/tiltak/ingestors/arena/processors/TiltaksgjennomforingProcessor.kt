package no.nav.amt.tiltak.ingestors.arena.processors

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.stereotype.Component

@Component
class TiltaksgjennomforingProcessor(
	repository: ArenaDataRepository,
	val tiltaksleverandorService: TiltaksleverandorService,
	val tiltakService: TiltakService,
	val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	override fun insert(data: ArenaData) {
		val newFields = jsonObject(data.after)

		val virksomhet = addVirksomhet(newFields)
	}

	override fun update(data: ArenaData) {
		TODO("Not yet implemented")
	}

	override fun delete(data: ArenaData) {
		TODO("Not yet implemented")
	}

	private fun addVirksomhet(fields: ArenaTiltaksgjennomforing): Virksomhet {
		val virksomhetsnummer = ords.hentVirksomhetsnummer(fields.ARBGIV_ID_ARRANGOR.toString())
		return tiltaksleverandorService.addVirksomhet(virksomhetsnummer)
	}

	private fun addTiltak(virksomhet: Virksomhet, fields: ArenaTiltaksgjennomforing): Tiltak {
		val unsavedTiltak = Tiltak(
			id = null,
			tiltaksleverandorId = virksomhet.id!!,
			navn = fields.LOKALTNAVN!!,
			kode = fields.TILTAKSKODE
		)

		return tiltakService.addTiltak(unsavedTiltak)
	}

	// TODO generaliser
	private fun jsonObject(s: String?): ArenaTiltaksgjennomforing {
		if (s == null) {
			throw UnsupportedOperationException("Expected AreanaTiltaksgjennomforing not to be null!")
		}

		return ObjectMapper().readValue(s, ArenaTiltaksgjennomforing::class.java)
	}
}
