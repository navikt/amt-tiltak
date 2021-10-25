package no.nav.amt.tiltak.tiltak.deltaker

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.tiltak.deltaker.dbo.NavAnsattDbo
import no.nav.amt.tiltak.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.tiltak.deltaker.repositories.DeltakerRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeltakerService(
	private val deltakerRepository: DeltakerRepository,
	private val brukerRepository: BrukerRepository
) {

	/**
	 * - Check if Deltaker in database
	 * - Check if Bruker in database
	 * - Get Brukerdata
	 * - Get Veilederdata
	 * - Connect Deltaker to tiltaksgjennomforing
	 */

	fun addUpdateDeltaker(tiltaksgjennomforing: UUID, fodselsnummer: String): Deltaker {
		val storedDeltaker = deltakerRepository.get(fodselsnummer, tiltaksgjennomforing)

		if (storedDeltaker != null) {
			return storedDeltaker.toDeltaker()
		}



		TODO("Not yet implemented")
	}

	private fun getBruker(fodselsnummer: String): BrukerDbo {
		val storedBruker = brukerRepository.get(fodselsnummer)

		if(storedBruker != null) {
			return storedBruker
		}
	}

	private fun getVeileder(fodselsnummer: String): NavAnsattDbo {
		TODO("Not yet implemented")
	}
}
