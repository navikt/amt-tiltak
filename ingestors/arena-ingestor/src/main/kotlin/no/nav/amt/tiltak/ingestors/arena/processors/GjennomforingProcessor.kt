package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaTiltakIgnoredRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
internal open class GjennomforingProcessor(
	repository: ArenaDataRepository,
	private val ignoredTiltakRepository: ArenaTiltakIgnoredRepository,
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val tiltakService: TiltakService,
	private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun insert(data: ArenaData) {
		upsert(data)
	}

	override fun update(data: ArenaData) {
		upsert(data)
	}

	private fun upsert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		if (!isSupportedTiltak(newFields.TILTAKSKODE)) {
			ignoredTiltakRepository.insert(newFields.TILTAKGJENNOMFORING_ID)
			repository.upsert(data.markAsIgnored())
			return
		}

		if (ugyldigGjennomforing(newFields)) {
			log.info("Hopper over upsert av tiltakgjennomforing som mangler data. arenaTiltakgjennomforingId=${newFields.TILTAKGJENNOMFORING_ID}")
			repository.upsert(data.markAsIgnored())
			return
		}

		val virksomhetsnummer = ords.hentVirksomhetsnummer(newFields.ARBGIV_ID_ARRANGOR.toString())

		val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
			?: throw DependencyNotIngestedException("Tiltak med ID ${newFields.TILTAKSKODE} er ikke ingested enda.")

		val arrangor = arrangorService.addArrangor(virksomhetsnummer)

		gjennomforingService.upsertGjennomforing(
			arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
			tiltakId = tiltak.id,
			arrangorId = arrangor.id,
			navn = newFields.LOKALTNAVN.toString(),
			status = null,
			oppstartDato = newFields.DATO_FRA?.asLocalDate(),
			sluttDato = newFields.DATO_TIL?.asLocalDate(),
			registrertDato = newFields.REG_DATO.asLocalDateTime(),
			fremmoteDato = newFields.DATO_FREMMOTE?.asLocalDate() withTime newFields.KLOKKETID_FREMMOTE.asTime()
		)
		repository.upsert(data.markAsIngested())


	}

	override fun delete(data: ArenaData) {
		log.error("Delete is not implemented for TiltaksgjennomforingProcessor")
		repository.upsert(data.markAsFailed())
	}

	private fun ugyldigGjennomforing(data: ArenaTiltaksgjennomforing) =
		data.ARBGIV_ID_ARRANGOR == null || data.LOKALTNAVN == null

}
