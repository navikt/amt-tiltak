package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(
		id: UUID,
		gjennomforingId: UUID,
		fodselsnummer: String,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		dagerPerUke: Int?,
		prosentStilling: Float?,
		registrertDato: LocalDateTime
	): Deltaker

	fun hentDeltakerePaaGjennomforing(id: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker

	fun oppdaterDeltakerVeileder(brukerPersonligIdent: String, veilederId: UUID)

}
