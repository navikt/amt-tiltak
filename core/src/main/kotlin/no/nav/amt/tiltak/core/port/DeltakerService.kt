package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.util.*

interface DeltakerService {
	fun addUpdateDeltaker(
		tiltaksinstans: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status = Deltaker.Status.NY_BRUKER,
		arenaStatus: String?,
		dagerPerUke: Int?,
		prosentStilling: Float?
	): Deltaker

	fun hentDeltakerePaaTiltak(id: UUID): List<Deltaker>
}
