package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.*
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerUpdateDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.tiltak.services.BrukerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
	private val transactionTemplate: TransactionTemplate
) : DeltakerService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsertDeltaker(fodselsnummer: String, deltaker: DeltakerUpsert) {
		val lagretDeltaker = hentDeltaker(deltaker.id)

		if (lagretDeltaker == null) {
			insertDeltaker(fodselsnummer, deltaker)
		} else if(!deltaker.compareTo(lagretDeltaker)){
			update(deltaker)
		}
	}

	override fun insertStatus(status: DeltakerStatusInsert) {
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(status.deltakerId)
		if(forrigeStatus?.status == status.type) return

		val nyStatus = DeltakerStatusInsertDbo(
			id = status.id,
			deltakerId = status.deltakerId,
			type = status.type,
			gyldigFra = status.gyldigFra?: LocalDateTime.now()
		)
		transactionTemplate.executeWithoutResult {
			forrigeStatus?.let { deltakerStatusRepository.deaktiver(it.id) }
			deltakerStatusRepository.insert(nyStatus)
		}
	}

	override fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(gjennomforingId)
			.map { deltaker ->
				return@map deltaker.toDeltaker(hentStatusOrThrow(deltaker.id))
			}
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker? {
		val deltaker = deltakerRepository.get(deltakerId)
			?: return null

		return deltaker.toDeltaker(hentStatusOrThrow(deltakerId))
	}

	override fun hentDeltaker(fodselsnummer: String): Deltaker? {
		val deltaker = deltakerRepository.get(fodselsnummer)
			?: return null
		return deltaker.toDeltaker(hentStatusOrThrow(deltaker.id))
	}

	override fun oppdaterStatuser() {
		progressStatuser(deltakerRepository.potensieltHarSlutta())
		progressStatuser(deltakerRepository.potensieltDeltar())
	}

	override fun slettDeltaker(deltakerId: UUID) {
		transactionTemplate.execute {
			deltakerStatusRepository.slettDeltakerStatus(deltakerId)
			deltakerRepository.slettDeltaker(deltakerId)
		}

		log.info("Deltaker med id=$deltakerId er slettet")
	}

	override fun oppdaterNavEnhet(fodselsnummer: String, navEnhet: NavEnhet?) {
		brukerService.oppdaterNavEnhet(fodselsnummer, navEnhet)
	}

	private fun update(deltaker: DeltakerUpsert) {
		val toUpdate = DeltakerUpdateDbo(
			id = deltaker.id,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			registrertDato = deltaker.registrertDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			innsokBegrunnelse = deltaker.innsokBegrunnelse
		)

		deltakerRepository.update(toUpdate)
	}

	private fun insertDeltaker(fodselsnummer: String, deltaker: DeltakerUpsert) {
		val brukerId = brukerService.getOrCreate(fodselsnummer)
		val toInsert = DeltakerInsertDbo(
			id = deltaker.id,
			brukerId = brukerId,
			gjennomforingId = deltaker.gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato,
			innsokBegrunnelse = deltaker.innsokBegrunnelse
		)

		deltakerRepository.insert(toInsert)
	}

	private fun hentStatusOrThrow(deltakerId: UUID) : DeltakerStatus {
		return hentStatus(deltakerId)?: throw NoSuchElementException("Fant ikke status på deltaker med id $deltakerId")
	}

	private fun hentStatus(deltakerId: UUID) : DeltakerStatus? {
		return deltakerStatusRepository.getStatusForDeltaker(deltakerId)?.toDeltakerStatus() ?: return null
	}

	private fun progressStatuser(kandidater: List<DeltakerDbo>) = kandidater
		.also { log.info("Oppdaterer status på ${it.size} deltakere") }
		.map { deltaker ->
			deltaker.toDeltaker(hentStatusOrThrow(deltaker.id))
		}
		.forEach {
			insertStatus(DeltakerStatusInsert(
				id = UUID.randomUUID(),
				deltakerId = it.id,
				type = it.utledStatus(),
				gyldigFra = LocalDateTime.now()
			))
		}

	override fun finnesBruker(fodselsnummer: String): Boolean {
		return brukerService.finnesBruker(fodselsnummer)
	}

	override fun oppdaterAnsvarligVeileder(fodselsnummer: String, navAnsattId: UUID) {
		brukerService.oppdaterAnsvarligVeileder(fodselsnummer, navAnsattId)
	}


}


