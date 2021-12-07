package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavKontorService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.deltaker.commands.UpsertNavAnsattCommand
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.NavAnsattDbo
import no.nav.amt.tiltak.deltaker.dbo.NavKontorDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.NavAnsattRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val brukerRepository: BrukerRepository,
	private val navAnsattRepository: NavAnsattRepository,
	private val navKontorRepository: NavKontorRepository,
	private val navKontorService: NavKontorService,
	private val personService: PersonService,
) : DeltakerService {

	override fun addUpdateDeltaker(
		tiltaksinstans: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		arenaStatus: String?,
		dagerPerUke: Int?,
		prosentStilling: Float?
	): Deltaker {
		val storedDeltaker = deltakerRepository.get(fodselsnummer, tiltaksinstans)

		if (storedDeltaker != null) {
			return storedDeltaker.toDeltaker()
		}

		val bruker = getBruker(fodselsnummer)

		val deltaker = deltakerRepository.get(
			fodselsnummer = fodselsnummer,
			tiltaksinstansId = tiltaksinstans
		)


		if (deltaker != null) {
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
		} else {
			return deltakerRepository.insert(
				brukerId = bruker.id,
				tiltaksgjennomforingId = tiltaksinstans,
				oppstartDato = oppstartDato,
				sluttDato = sluttDato,
				status = status,
				arenaStatus = arenaStatus,
				dagerPerUke = dagerPerUke,
				prosentStilling = prosentStilling
			).toDeltaker()

		}
	}

	override fun hentDeltakerePaaTiltakInstans(id: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltakInstans(id).map { it.toDeltaker() }
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker {
		return deltakerRepository.get(deltakerId)?.toDeltaker()
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
	}

	private fun getBruker(fodselsnummer: String): BrukerDbo {
		val storedBruker = brukerRepository.get(fodselsnummer)

		if (storedBruker != null) {
			return storedBruker
		}

		val veileder = upsertVeileder(fodselsnummer)
		val navKontor = getNavKontor(fodselsnummer)
		val newBruker = personService.hentPerson(fodselsnummer)

		return brukerRepository.insert(
			fodselsnummer = fodselsnummer,
			fornavn = newBruker.fornavn,
			mellomnavn = newBruker.mellomnavn,
			etternavn = newBruker.etternavn,
			telefonnummer = newBruker.telefonnummer,
			epost = null,
			ansvarligVeilederId = veileder?.id,
			navKontorId = navKontor?.id
		)
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

	private fun getNavKontor(fodselsnummer: String): NavKontorDbo? {
		return navKontorService.hentNavKontorForBruker(fodselsnummer)?.let {
			navKontorRepository.upsert(it.enhetId, it.navn)
		}
	}


}
