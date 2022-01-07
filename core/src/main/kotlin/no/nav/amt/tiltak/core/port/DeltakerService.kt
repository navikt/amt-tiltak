package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(
		gjennomforingId: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker

	fun upsertDeltaker(
		id: UUID,
		gjennomforingId: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker

	fun hentDeltakerePaaGjennomforing(id: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker

}
