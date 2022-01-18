package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(
		fodselsnummer: String,
		gjennomforingId: UUID,
		deltaker: Deltaker,
	): Deltaker

	fun hentDeltakerePaaGjennomforing(id: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker

	fun oppdaterStatuser()

}
