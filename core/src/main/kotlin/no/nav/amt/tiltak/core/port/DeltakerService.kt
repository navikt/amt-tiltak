package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(fodselsnummer: String, deltaker: DeltakerUpsert)

	fun insertStatus(status: DeltakerStatusInsert)

	fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker?

	fun oppdaterStatuser()

	fun slettDeltaker(deltakerId: UUID)

}
