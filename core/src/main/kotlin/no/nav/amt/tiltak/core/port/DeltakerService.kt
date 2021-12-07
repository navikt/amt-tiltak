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
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?
	): Deltaker

	fun hentDeltakerePaaTiltakInstans(id: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker

}
