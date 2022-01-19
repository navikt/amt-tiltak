package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavKontorService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.NavKontorDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val brukerRepository: BrukerRepository,
	private val navKontorRepository: NavKontorRepository,
	private val navKontorService: NavKontorService,
	private val personService: PersonService,
	private val veilederService: VeilederService,
) : DeltakerService {

	override fun upsertDeltaker(
		id: UUID,
		gjennomforingId: UUID,
		fodselsnummer: String,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker {
		deltakerRepository.get(id)?.also { deltaker ->
			val updated = deltaker.update(
				newStatus = status,
				newDeltakerStartDato = startDato,
				newDeltakerSluttDato = sluttDato
			)

			return if (updated.status == UpdateStatus.UPDATED) {
				deltakerRepository.update(updated.updatedObject!!).toDeltaker()
			} else {
				deltaker.toDeltaker()
			}
		}

		return createDeltaker(
			id,
			fodselsnummer,
			gjennomforingId,
			startDato,
			sluttDato,
			status,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)
	}

	private fun createDeltaker(
		deltakerId: UUID,
		fodselsnummer: String,
		gjennomforingId: UUID,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker {
		val bruker = brukerRepository.get(fodselsnummer) ?: createBruker(fodselsnummer)

		return deltakerRepository.insert(
			id = deltakerId,
			brukerId = bruker.id,
			gjennomforingId = gjennomforingId,
			startDato = startDato,
			sluttDato = sluttDato,
			status = status,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato
		).toDeltaker()
	}

	override fun hentDeltakerePaaGjennomforing(id: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(id).map { it.toDeltaker() }
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker {
		return deltakerRepository.get(deltakerId)?.toDeltaker()
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
	}

	override fun finnesBruker(fodselsnummer: String): Boolean {
		return brukerRepository.get(fodselsnummer) != null
	}

	override fun oppdaterDeltakerVeileder(brukerPersonligIdent: String, veilederId: UUID) {
		brukerRepository.oppdaterVeileder(brukerPersonligIdent, veilederId)
	}

	private fun createBruker(fodselsnummer: String): BrukerDbo {
		val veilederId = upsertVeileder(fodselsnummer)

		val navKontor = getNavKontor(fodselsnummer)

		val personKontaktinformasjon = personService.hentPersonKontaktinformasjon(fodselsnummer)

		val person = personService.hentPerson(fodselsnummer)

		val bruker = BrukerInsertDbo(
			fodselsnummer = fodselsnummer,
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn,
			telefonnummer = person.telefonnummer ?: personKontaktinformasjon.telefonnummer,
			epost = personKontaktinformasjon.epost,
			ansvarligVeilederId = veilederId,
			navKontorId = navKontor?.id
		)

		return brukerRepository.insert(bruker)
	}

	private fun upsertVeileder(fodselsnummer: String): UUID? {
		return personService.hentTildeltVeileder(fodselsnummer)?.let { veileder ->
			return veilederService.upsertVeileder(veileder)
		}
	}

	private fun getNavKontor(fodselsnummer: String): NavKontorDbo? {
		return navKontorService.hentNavKontorForBruker(fodselsnummer)?.let {
			navKontorRepository.upsert(it.enhetId, it.navn)
		}
	}

}
