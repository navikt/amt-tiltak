package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import java.util.UUID

interface ArrangorVeilederService {

	fun hentVeiledereForDeltaker(deltakerId: UUID): List<ArrangorVeileder>

	fun hentDeltakereForVeileder(ansattId: UUID): List<ArrangorVeileder>

	fun erVeilederFor(ansattId: UUID, deltakerId: UUID): Boolean

	fun opprettVeiledereForDeltaker(veiledere: List<ArrangorVeilederInput>, deltakerId: UUID)

	fun fjernAlleDeltakereForVeilederHosArrangor(ansattId: UUID, arrangorId: UUID)

	fun leggTilAnsattSomVeileder(ansattId: UUID, deltakerId: UUID, erMedveileder: Boolean)

	fun fjernAnsattSomVeileder(ansattId: UUID, deltakerId: UUID, erMedveileder: Boolean)
}
