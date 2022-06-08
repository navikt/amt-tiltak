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
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
	private val transactionTemplate: TransactionTemplate
) : DeltakerService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsertDeltaker(fodselsnummer: String, deltaker: Deltaker) {
		val lagretDeltakerDbo = deltakerRepository.get(deltaker.id)

		if (lagretDeltakerDbo == null) {
			createDeltaker(fodselsnummer, deltaker.gjennomforingId, deltaker)
		} else {
			val lagretDeltaker = lagretDeltakerDbo.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker)
			val oppdatertDeltaker = lagretDeltaker.oppdater(deltaker)

			if (lagretDeltaker != oppdatertDeltaker) {
				deltakerStatusRepository.upsert(DeltakerStatusDbo.fromDeltaker(oppdatertDeltaker))
				update(oppdatertDeltaker)
			}
		}
	}


	private fun update(deltaker: Deltaker): Deltaker {

		return deltakerRepository.update(DeltakerDbo(deltaker))
			.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker)
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

	override fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(gjennomforingId)
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

	override fun slettDeltaker(deltakerId: UUID) {
		transactionTemplate.execute {
			deltakerStatusRepository.slettDeltakerStatus(deltakerId)
			deltakerRepository.slettDeltaker(deltakerId)
		}

		log.info("Deltaker med id=$deltakerId er slettet")
	}

	private fun progressStatuser(kandidatProvider: () -> List<DeltakerDbo>) = kandidatProvider()
		.also { log.info("Oppdaterer status på ${it.size} deltakere ") }
		.map { it.toDeltaker(deltakerStatusRepository::getStatuserForDeltaker) }
		.map { it.progressStatus() }
		.forEach { deltakerStatusRepository.upsert( DeltakerStatusDbo.fromDeltaker(it)) }

}

