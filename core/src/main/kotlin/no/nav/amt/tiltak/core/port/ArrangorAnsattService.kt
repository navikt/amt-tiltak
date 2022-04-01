package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import java.util.*

interface ArrangorAnsattService {

	fun opprettAnsattHvisIkkeFinnes(ansatttPersonIdent: String): Ansatt

	fun getAnsatt(ansattId: UUID): Ansatt

 	fun getAnsattByPersonligIdent(personIdent: String): Ansatt?

}
