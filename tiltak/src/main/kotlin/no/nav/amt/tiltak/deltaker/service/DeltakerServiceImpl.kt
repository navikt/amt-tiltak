package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
) : DeltakerService {

	companion object {
		private val log = LoggerFactory.getLogger(DeltakerService::class.java)
	}

	override fun upsertDeltaker(fodselsnummer: String, gjennomforingId: UUID, deltaker: Deltaker): Deltaker {

		return deltakerRepository.get(fodselsnummer, gjennomforingId)
			?.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker)
			?.let {
				val updated = it.updateStatus(deltaker.status, deltaker.startDato, deltaker.sluttDato)
				if(it != updated) update(updated) else it
			}
			?: createDeltaker(fodselsnummer, gjennomforingId, deltaker)
	}

	private fun update(deltaker: Deltaker): Deltaker {

		return deltakerRepository.update(DeltakerDbo(deltaker))
			.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker)
	}

	private fun createDeltaker(fodselsnummer: String, gjennomforingId: UUID, deltaker: Deltaker): Deltaker {
		val brukerId = brukerService.getOrCreate(fodselsnummer)

		deltakerStatusRepository.upsert(DeltakerStatusDbo.fromDeltaker(deltaker) )

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

	override fun oppdaterStatuser() {
		progressStatuser(deltakerRepository::potensieltHarSlutta)
		progressStatuser(deltakerRepository::potensieltDeltar)
	}

	private fun progressStatuser(kandidatProvider: () -> List<DeltakerDbo>) = kandidatProvider()
		.also { log.info("Oppdaterer status på ${it.size} deltakere ") }
		.map { it.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker) }
		.map { it.progressStatus() }
		.forEach { deltakerStatusRepository.upsert( DeltakerStatusDbo.fromDeltaker(it)) }

}

