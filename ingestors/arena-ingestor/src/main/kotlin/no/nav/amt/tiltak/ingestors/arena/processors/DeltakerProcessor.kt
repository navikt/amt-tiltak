package no.nav.amt.tiltak.ingestors.arena.processors

import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.dto.ArenaTiltakDeltaker
import no.nav.amt.tiltak.ingestors.arena.exceptions.DependencyNotIngestedException
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaTiltakIgnoredRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
internal open class DeltakerProcessor(
	repository: ArenaDataRepository,
	private val ignoredTiltakRepository: ArenaTiltakIgnoredRepository,
	private val tiltakInstansService: TiltakInstansService,
	private val deltakerService: DeltakerService,
	private val ords: ArenaOrdsProxyConnector
) : AbstractArenaProcessor(repository) {

	private val logger = LoggerFactory.getLogger(javaClass)

	override fun insert(data: ArenaData) {
		upsert(data)
	}

	override fun update(data: ArenaData) {
		upsert(data)
	}

	override fun delete(data: ArenaData) {
		logger.error("Delete is not implemented for DeltakerProcessor")
		repository.upsert(data.markAsFailed())
	}

	private fun upsert(data: ArenaData) {
		val newFields = jsonObject(data.after, ArenaTiltakDeltaker::class.java)

		if (ignoredTiltakRepository.contains(newFields.TILTAKGJENNOMFORING_ID)) {
			repository.upsert(data.markAsIgnored())
		} else {
			val tiltaksgjennomforing =
				tiltakInstansService.getTiltaksinstansFromArenaId(newFields.TILTAKGJENNOMFORING_ID.toInt())
					?: throw DependencyNotIngestedException("Tiltaksgjennomføring med ID ${newFields.TILTAKGJENNOMFORING_ID} er ikke ingested.")

			val fodselsnummer = ords.hentFnr(newFields.PERSON_ID.toString())
				?: throw DataIntegrityViolationException("Person med Arena ID ${newFields.PERSON_ID} returnerer ikke fødselsnummer")

			deltakerService.addUpdateDeltaker(
				tiltaksinstans = tiltaksgjennomforing.id,
				fodselsnummer = fodselsnummer,
				oppstartDato = newFields.DATO_FRA?.asLocalDate(),
				sluttDato = newFields.DATO_TIL?.asLocalDate(),
				arenaStatus = newFields.DELTAKERSTATUSKODE,
				dagerPerUke = newFields.ANTALL_DAGER_PR_UKE,
				prosentStilling = newFields.PROSENT_DELTID,
			)

			repository.upsert(data.markAsIngested())
		}

	}
}
