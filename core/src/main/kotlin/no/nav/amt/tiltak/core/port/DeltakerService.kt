package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface DeltakerService {

	fun addUpdateDeltaker(
		gjennomforingId: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker

	fun hentDeltakerePaaTiltak(id: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker

}
