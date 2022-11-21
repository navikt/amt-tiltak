package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.*
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.SkjermetPersonService
import no.nav.amt.tiltak.deltaker.dbo.*
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.tiltak.services.BrukerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
	private val endringsmeldingService: EndringsmeldingService,
	private val skjermetPersonService: SkjermetPersonService,
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
		if (forrigeStatus?.type == status.type && forrigeStatus.aarsak == status.aarsak) return

		val nyStatus = DeltakerStatusInsertDbo(
			id = status.id,
			deltakerId = status.deltakerId,
			type = status.type,
			aarsak = status.aarsak,
			gyldigFra = status.gyldigFra?: LocalDateTime.now()
		)
		transactionTemplate.executeWithoutResult {
			forrigeStatus?.let { deltakerStatusRepository.deaktiver(it.id) }
			deltakerStatusRepository.insert(nyStatus)
		}
	}

	override fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker> {
		val deltakere = deltakerRepository.getDeltakerePaaTiltak(gjennomforingId)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker? {
		val deltaker = deltakerRepository.get(deltakerId)
			?: return null

		return deltaker.toDeltaker(hentStatusOrThrow(deltakerId))
	}

	override fun hentDeltakereMedFnr(fodselsnummer: String): List<Deltaker>{
		val deltakere =  deltakerRepository.getDeltakereMedFnr(fodselsnummer)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun oppdaterStatuser() {
		oppdaterStatuser(deltakerRepository.skalAvsluttes(), DeltakerStatus.Type.HAR_SLUTTET)
		oppdaterStatuser(deltakerRepository.erPaaAvsluttetGjennomforing(), DeltakerStatus.Type.HAR_SLUTTET)
		oppdaterStatuser(deltakerRepository.skalHaStatusDeltar(), DeltakerStatus.Type.DELTAR)
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

	override fun erSkjermet(deltakerId: UUID) : Boolean {
		val deltaker = hentDeltaker(deltakerId)?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
		return skjermetPersonService.erSkjermet(deltaker.fodselsnummer)
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
		return deltakerStatusRepository.getStatusForDeltaker(deltakerId)?.toModel() ?: return null
	}

	private fun hentAktiveStatuserForDeltakere(deltakerIder: List<UUID>): Map<UUID, DeltakerStatusDbo> {
		return deltakerStatusRepository.getAktiveStatuserForDeltakere(deltakerIder).associateBy { it.deltakerId }
	}

	private fun mapDeltakereOgAktiveStatuser(deltakere: List<DeltakerDbo>): List<Deltaker> {
		val statuser = hentAktiveStatuserForDeltakere(deltakere.map { it.id })
		return deltakere.map { d ->
			val status = statuser[d.id] ?: throw NoSuchElementException("Fant ikke status på deltaker med id ${d.id}")
			return@map d.toDeltaker(status.toModel())
		}
	}

	private fun oppdaterStatuser(deltakere: List<DeltakerDbo>, type: DeltakerStatus.Type) = deltakere
		.also { log.info("Oppdaterer status på ${it.size} deltakere") }
		.forEach {
			insertStatus(DeltakerStatusInsert(
				id = UUID.randomUUID(),
				deltakerId = it.id,
				type = type,
				aarsak = null,
				gyldigFra = LocalDateTime.now()
			))
		}

	override fun finnesBruker(fodselsnummer: String): Boolean {
		return brukerService.finnesBruker(fodselsnummer)
	}

	override fun oppdaterAnsvarligVeileder(fodselsnummer: String, navAnsattId: UUID) {
		brukerService.oppdaterAnsvarligVeileder(fodselsnummer, navAnsattId)
	}

	override fun leggTilOppstartsdato(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate) {
		endringsmeldingService.opprettLeggTilOppstartsdatoEndringsmelding(deltakerId, arrangorAnsattId, oppstartsdato)
	}

	override fun endreOppstartsdato(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate) {
		endringsmeldingService.opprettEndreOppstartsdatoEndringsmelding(deltakerId, arrangorAnsattId, oppstartsdato)
	}

	override fun forlengDeltakelse(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate) {
		endringsmeldingService.opprettForlengDeltakelseEndringsmelding(deltakerId, arrangorAnsattId, sluttdato)
	}

	override fun avsluttDeltakelse(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate,
		statusAarsak: DeltakerStatus.Aarsak
	) {
		endringsmeldingService.opprettAvsluttDeltakelseEndringsmelding(deltakerId, arrangorAnsattId, sluttdato, statusAarsak)
	}

	override fun deltakerIkkeAktuell(deltakerId: UUID, arrangorAnsattId: UUID, statusAarsak: DeltakerStatus.Aarsak) {
		endringsmeldingService.opprettDeltakerIkkeAktuellEndringsmelding(deltakerId, arrangorAnsattId, statusAarsak)
	}
}


