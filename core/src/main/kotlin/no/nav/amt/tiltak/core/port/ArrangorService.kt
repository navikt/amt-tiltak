package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import java.util.*

interface ArrangorService {

	fun addArrangor(virksomhetsnummer: String): Arrangor

	fun getVirksomheterForAnsatt(ansattId: UUID): List<Arrangor>
	fun getAnsatt(ansattId: UUID): Ansatt
	fun getAnsattByPersonligIdent(personIdent: String): Ansatt
	fun getArrangorByVirksomhetsnummer(virksomhetsnummer: String): Arrangor?
}
