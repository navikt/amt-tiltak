package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import java.util.*

interface ArrangorVeilederService {

	fun opprettVeiledere(veilederInputs: List<ArrangorVeilederInput>, deltakerIder: List<UUID>)

	fun hentVeiledereForDeltaker(deltakerId: UUID): List<ArrangorVeileder>

	fun hentAktiveVeiledereForGjennomforing(gjennomforingId: UUID): List<ArrangorVeileder>

	fun hentTilgjengeligeVeiledereForGjennomforing(gjennomforingId: UUID): List<Ansatt>

	fun erVeilederFor(ansattId: UUID, deltakerId: UUID): Boolean

	fun opprettVeiledereForDeltaker(veiledere: List<ArrangorVeilederInput>, deltakerId: UUID)

}
