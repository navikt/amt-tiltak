package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaTiltakIgnoredRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
open class TiltaksgjennomforingProcessor(
	repository: ArenaDataRepository,
	private val ignoredTiltakRepository: ArenaTiltakIgnoredRepository,
	private val tiltaksleverandorService: TiltaksleverandorService,
	private val tiltakService: TiltakService,
	private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	override fun insert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		if (isSupportedTiltak(newFields.TILTAKSKODE)) {
			val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
				?: throw DependencyNotIngestedException("Tiltak med ID ${newFields.TILTAKSKODE} er ikke ingested.")

			val tiltaksleverandor = addTiltaksleverandor(newFields)

			tiltakService.upsertTiltaksinstans(
				arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
				tiltakId = tiltak.id,
				tiltaksleverandorId = tiltaksleverandor.id,
				navn = newFields.LOKALTNAVN
					?: throw DataIntegrityViolationException("Forventet at LOKALTNAVN ikke er null"),
				status = null,
				oppstartDato = newFields.DATO_FRA?.asLocalDate(),
				sluttDato = newFields.DATO_TIL?.asLocalDate(),
				registrertDato = newFields.REG_DATO.asLocalDateTime(),
				fremmoteDato = newFields.DATO_FREMMOTE?.asLocalDate() withTime newFields.KLOKKETID_FREMMOTE.asTime()
			)
		} else {
			ignoredTiltakRepository.insert(newFields.TILTAKGJENNOMFORING_ID)
			repository.update(data.markAsIgnored())
			repository.markAsIgnored(data.id)
		}
	}

	override fun update(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		if (isSupportedTiltak(newFields.TILTAKSKODE)) {
			val virksomhetsnummer = ords.hentVirksomhetsnummer(newFields.ARBGIV_ID_ARRANGOR.toString())

			val tiltaksleverandor = tiltaksleverandorService.getTiltaksleverandorByVirksomhetsnummer(virksomhetsnummer)
				?: throw DependencyNotIngestedException("Tiltaksleverandør med virksomhetsnummer $virksomhetsnummer er ikke ingested enda.")

			val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
				?: throw DependencyNotIngestedException("Tilktak med ArenaId $virksomhetsnummer er ikke ingested enda.")


			tiltakService.upsertTiltaksinstans(
				arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
				tiltakId = tiltak.id,
				tiltaksleverandorId = tiltaksleverandor.id,
				navn = newFields.LOKALTNAVN
					?: throw DataIntegrityViolationException("Forventet at LOKALTNAVN ikke er null"),
				status = null,
				oppstartDato = newFields.DATO_FRA?.asLocalDate(),
				sluttDato = newFields.DATO_TIL?.asLocalDate(),
				registrertDato = newFields.REG_DATO.asLocalDateTime(),
				fremmoteDato = newFields.DATO_FREMMOTE?.asLocalDate() withTime newFields.KLOKKETID_FREMMOTE.asTime()
			)
		}
	}

	override fun delete(data: ArenaData) {
		TODO("Not yet implemented")
	}

	private fun addTiltaksleverandor(fields: ArenaTiltaksgjennomforing): Tiltaksleverandor {
		val virksomhetsnummer = ords.hentVirksomhetsnummer(fields.ARBGIV_ID_ARRANGOR.toString())
		return tiltaksleverandorService.addTiltaksleverandor(virksomhetsnummer)
	}
}
