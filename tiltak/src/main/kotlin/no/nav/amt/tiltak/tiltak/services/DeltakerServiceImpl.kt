package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.tiltak.deltaker.cmd.UpsertNavAnsattCmd
import no.nav.amt.tiltak.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.tiltak.deltaker.dbo.NavAnsattDbo
import no.nav.amt.tiltak.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.tiltak.deltaker.repositories.NavAnsattRepository
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val brukerRepository: BrukerRepository,
	private val navAnsattRepository: NavAnsattRepository,
	private val personService: PersonService,
): DeltakerService {

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

	override fun hentDeltakerePaaTiltak(id: UUID): List<Deltaker> {
		return deltakerRepository.getDeltakerePaaTiltak(id).map { it.toDeltaker() }
	}

	private fun getBruker(fodselsnummer: String): BrukerDbo {
		val storedBruker = brukerRepository.get(fodselsnummer)

		if (storedBruker != null) {
			return storedBruker
		}

		val veileder = upsertVeileder(fodselsnummer)
		val newBruker = personService.hentPerson(fodselsnummer)

		return brukerRepository.insert(
			fodselsnummer = fodselsnummer,
			fornavn = newBruker.fornavn,
			mellomnavn = newBruker.mellomnavn,
			etternavn = newBruker.etternavn,
			telefonnummer = newBruker.telefonnummer,
			epost = null,
			ansvarligVeilederId = veileder?.id
		)

	}

	private fun upsertVeileder(fodselsnummer: String): NavAnsattDbo? {
		return personService.hentVeileder(fodselsnummer)?.let { veileder ->
			navAnsattRepository.upsert(UpsertNavAnsattCmd(
				personligIdent = veileder.navIdent,
				fornavn = veileder.fornavn,
				etternavn = veileder.etternavn,
				epost = veileder.epost,
				telefonnummer = "TODO - Ikke hentet fra NOM enda"
			))
			return navAnsattRepository.getNavAnsattWithIdent(veileder.navIdent)
		}
	}


}
