package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import java.util.*

interface ArrangorService {

	fun upsertArrangor(virksomhetsnummer: String): Arrangor

	fun getVirksomheterForAnsatt(ansattId: UUID): List<Arrangor>

	fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): Arrangor?

	fun getArrangorById(id: UUID): Arrangor

}
