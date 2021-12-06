package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.core.port.TiltaksarrangorService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltaksgjennomforing
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaTiltakIgnoredRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
open class TiltaksgjennomforingProcessor(
	repository: ArenaDataRepository,
	private val ignoredTiltakRepository: ArenaTiltakIgnoredRepository,
	private val tiltaksarrangorService: TiltaksarrangorService,
	private val tiltakInstansService: TiltakInstansService,
	private val tiltakService: TiltakService,
	private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun insert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		if (isSupportedTiltak(newFields.TILTAKSKODE)) {
			val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
				?: throw DependencyNotIngestedException("Tiltak med ID ${newFields.TILTAKSKODE} er ikke ingested.")

			if (newFields.ARBGIV_ID_ARRANGOR == null) {
				log.info("Hopper over insert av tiltak instans som mangler ARBGIV_ID_ARRANGOR. arenaTiltakgjennomforingId=${newFields.TILTAKGJENNOMFORING_ID}")
				repository.upsert(data.markAsIgnored())
				return
			}

			val virksomhetsnummer = ords.hentVirksomhetsnummer(newFields.ARBGIV_ID_ARRANGOR.toString())

			val tiltaksarrangor = tiltaksarrangorService.addTiltaksarrangor(virksomhetsnummer)

			tiltakInstansService.upsertTiltaksinstans(
				arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
				tiltakId = tiltak.id,
				tiltaksarrangorId = tiltaksarrangor.id,
				navn = newFields.LOKALTNAVN
					?: throw DataIntegrityViolationException("Forventet at LOKALTNAVN ikke er null"),
				status = null,
				oppstartDato = newFields.DATO_FRA?.asLocalDate(),
				sluttDato = newFields.DATO_TIL?.asLocalDate(),
				registrertDato = newFields.REG_DATO.asLocalDateTime(),
				fremmoteDato = newFields.DATO_FREMMOTE?.asLocalDate() withTime newFields.KLOKKETID_FREMMOTE.asTime()
			)

			repository.upsert(data.markAsIngested())

		} else {
			ignoredTiltakRepository.insert(newFields.TILTAKGJENNOMFORING_ID)
			repository.upsert(data.markAsIgnored())
		}
	}

	override fun update(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltaksgjennomforing::class.java)

		if (isSupportedTiltak(newFields.TILTAKSKODE)) {
			if (newFields.ARBGIV_ID_ARRANGOR == null) {
				log.info("Hopper over update av tiltak instans som mangler ARBGIV_ID_ARRANGOR. arenaTiltakgjennomforingId=${newFields.TILTAKGJENNOMFORING_ID}")
				repository.upsert(data.markAsIgnored())
				return
			}

			val virksomhetsnummer = ords.hentVirksomhetsnummer(newFields.ARBGIV_ID_ARRANGOR.toString())

			val tiltaksarrangor = tiltaksarrangorService.getTiltaksarrangorByVirksomhetsnummer(virksomhetsnummer)
				?: throw DependencyNotIngestedException("Tiltaksleverandør med virksomhetsnummer $virksomhetsnummer er ikke ingested enda.")

			val tiltak = tiltakService.getTiltakFromArenaId(newFields.TILTAKSKODE)
				?: throw DependencyNotIngestedException("Tilktak med ArenaId $virksomhetsnummer er ikke ingested enda.")

			tiltakInstansService.upsertTiltaksinstans(
				arenaId = newFields.TILTAKGJENNOMFORING_ID.toInt(),
				tiltakId = tiltak.id,
				tiltaksarrangorId = tiltaksarrangor.id,
				navn = newFields.LOKALTNAVN
					?: throw DataIntegrityViolationException("Forventet at LOKALTNAVN ikke er null"),
				status = null,
				oppstartDato = newFields.DATO_FRA?.asLocalDate(),
				sluttDato = newFields.DATO_TIL?.asLocalDate(),
				registrertDato = newFields.REG_DATO.asLocalDateTime(),
				fremmoteDato = newFields.DATO_FREMMOTE?.asLocalDate() withTime newFields.KLOKKETID_FREMMOTE.asTime()
			)

			repository.upsert(data.markAsIngested())
		} else {
			ignoredTiltakRepository.insert(newFields.TILTAKGJENNOMFORING_ID)
			repository.upsert(data.markAsIgnored())
		}
	}

	override fun delete(data: ArenaData) {
		log.error("Delete is not implemented for TiltaksgjennomforingProcessor")
		repository.upsert(data.markAsFailed())
	}

}
