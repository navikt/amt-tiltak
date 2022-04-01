package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(
		fodselsnummer: String,
		deltaker: Deltaker,
	)

	fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker

	fun oppdaterStatuser()

	fun slettDeltaker(deltakerId: UUID)

}
