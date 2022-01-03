package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.deltaker.commands.UpsertNavAnsattCommand
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.NavAnsattDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.NavAnsattRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val brukerRepository: BrukerRepository,
	private val navAnsattRepository: NavAnsattRepository,
	private val personService: PersonService,
) : DeltakerService {

	override fun addUpdateDeltaker(
		gjennomforingId: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker {

		deltakerRepository.get(fodselsnummer, gjennomforingId)?.also { deltaker ->
			val updated = deltaker.update(
				newStatus = status,
				newDeltakerStartDato = oppstartDato,
				newDeltakerSluttDato = sluttDato
			)

			return if (updated.status == UpdateStatus.UPDATED) {
				deltakerRepository.update(updated.updatedObject!!).toDeltaker()
			} else {
				deltaker.toDeltaker()
			}
		}

		return createDeltaker(
			fodselsnummer,
			gjennomforingId,
			oppstartDato,
			sluttDato,
			status,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

	}

	private fun createDeltaker(
		fodselsnummer: String,
		gjennomforingId: UUID,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker {
		val bruker = brukerRepository.get(fodselsnummer) ?: createBruker(fodselsnummer)

		return deltakerRepository.insert(
			brukerId = bruker.id,
			gjennomforingId = gjennomforingId,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			status = status,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato
		).toDeltaker()
	}

	override fun hentDeltakerePaaTiltak(id: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(id).map { it.toDeltaker() }
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker {
		return deltakerRepository.get(deltakerId)?.toDeltaker()
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
	}

	private fun createBruker(fodselsnummer: String): BrukerDbo {

		val veileder = upsertVeileder(fodselsnummer)
		val newBruker = personService.hentPerson(fodselsnummer)
		val bruker = BrukerInsertDbo(
			fodselsnummer = fodselsnummer,
			fornavn = newBruker.fornavn,
			mellomnavn = newBruker.mellomnavn,
			etternavn = newBruker.etternavn,
			telefonnummer = newBruker.telefonnummer,
			epost = null,
			ansvarligVeilederId = veileder?.id
		)
		return brukerRepository.insert(bruker)
	}

	private fun upsertVeileder(fodselsnummer: String): NavAnsattDbo? {
		return personService.hentTildeltVeileder(fodselsnummer)?.let { veileder ->
			navAnsattRepository.upsert(
				UpsertNavAnsattCommand(
					personligIdent = veileder.navIdent,
					navn = veileder.navn,
					epost = veileder.epost,
					telefonnummer = veileder.telefonnummer
				)
			)
			return navAnsattRepository.getNavAnsattWithIdent(veileder.navIdent)
		}
	}


}
