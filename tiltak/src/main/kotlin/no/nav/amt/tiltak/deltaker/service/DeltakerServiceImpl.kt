package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
) : DeltakerService {

	override fun upsertDeltaker(fodselsnummer: String, gjennomforingId: UUID, deltaker: Deltaker) {
		val lagretDeltakerDbo = deltakerRepository.get(fodselsnummer, gjennomforingId)

		if (lagretDeltakerDbo == null) {
			createDeltaker(fodselsnummer, gjennomforingId, deltaker)
		} else {
			val lagretDeltaker = lagretDeltakerDbo.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker)
			val oppdatertDeltaker = lagretDeltaker.updateStatus(deltaker.status, deltaker.startDato, deltaker.sluttDato)

			if (lagretDeltaker != oppdatertDeltaker) {
				update(oppdatertDeltaker)
			}
		}
	}

	private fun update(deltaker: Deltaker) {
		deltakerRepository.update(DeltakerDbo(deltaker))
		deltakerStatusRepository.upsert(DeltakerStatusDbo.fromDeltaker(deltaker))
	}

	private fun createDeltaker(fodselsnummer: String, gjennomforingId: UUID, deltaker: Deltaker): DeltakerDbo {
		val brukerId = brukerService.getOrCreate(fodselsnummer)

		val dbo = deltakerRepository.insert(
			id = deltaker.id,
			brukerId = brukerId,
			gjennomforingId = gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato
		)

		deltakerStatusRepository.upsert(DeltakerStatusDbo.fromDeltaker(deltaker) )

		return dbo
	}

	override fun hentDeltakerePaaGjennomforing(id: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(id)
			.map { it.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker) }
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker {
		return deltakerRepository.get(deltakerId)?.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker)
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
	}

}

