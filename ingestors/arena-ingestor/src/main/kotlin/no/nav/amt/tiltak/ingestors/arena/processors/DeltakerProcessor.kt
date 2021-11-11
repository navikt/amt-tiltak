package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltakDeltaker
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
open class DeltakerProcessor(
	repository: ArenaDataRepository,
	private val tiltakService: TiltakService,
	private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	override fun insert(data: ArenaData) {
		addUpdate(data)
	}

	override fun update(data: ArenaData) {
		addUpdate(data)
	}

	override fun delete(data: ArenaData) {
		throw NotImplementedError("Delete not yet implemented on DeltakerProcessor")
	}

	private fun addUpdate(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltakDeltaker::class.java)

		val tiltaksgjennomforing = tiltakService.getTiltaksinstansFromArenaId(newFields.TILTAKGJENNOMFORING_ID.toInt())
			?: throw DependencyNotIngestedException("Tiltaksgjennomføring med ID ${newFields.TILTAKGJENNOMFORING_ID} er ikke ingested.")

		val fodselsnummer = ords.hentFnr(newFields.PERSON_ID.toString())
			?: throw DataIntegrityViolationException("Person med Arena ID ${newFields.PERSON_ID} returnerer ikke fødselsnummer")

		tiltakService.upsertDeltaker(
			tiltaksgjennomforing = tiltaksgjennomforing.id,
			fodselsnummer = fodselsnummer,
			oppstartDato = newFields.DATO_FRA?.asLocalDate(),
			sluttDato = newFields.DATO_TIL?.asLocalDate()
		)

	}
}
