package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import java.util.*

interface ArrangorVeilederService {

	fun opprettVeiledere(veilederInputs: List<ArrangorVeilederInput>, deltakerIder: List<UUID>)

	fun hentVeiledereForDeltaker(deltakerId: UUID): List<ArrangorVeileder>

}
