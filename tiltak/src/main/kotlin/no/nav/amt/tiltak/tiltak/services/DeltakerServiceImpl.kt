package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val brukerService: BrukerService
) : DeltakerService {

	override fun upsertDeltaker(fodselsnummer: String, gjennomforingId: UUID, deltaker: Deltaker): Deltaker {

		return deltakerRepository.get(fodselsnummer, gjennomforingId)?.toDeltaker()
			?.let {
				val updated = it.updateStatus(deltaker.status, deltaker.startDato, deltaker.sluttDato)
				if(it != updated) update(updated) else it
			}
			?: createDeltaker(fodselsnummer, gjennomforingId, deltaker)
	}

	private fun update(deltaker: Deltaker): Deltaker {

		return deltakerRepository.update(DeltakerDbo(deltaker)).toDeltaker()
	}

	private fun createDeltaker(fodselsnummer: String, gjennomforingId: UUID, deltaker: Deltaker): Deltaker {
		val bruker = brukerService.getOrCreate(fodselsnummer)

		return deltakerRepository.insert(
			id = deltaker.id,
			brukerId = bruker.id,
			gjennomforingId = gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			status = deltaker.status,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato
		).toDeltaker()
	}

	override fun hentDeltakerePaaGjennomforing(id: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(id).map { it.toDeltaker() }
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker {
		return deltakerRepository.get(deltakerId)?.toDeltaker()
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
	}


}

