package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Tiltaksarrangor
import java.util.*

interface TiltaksarrangorService {

	fun addTiltaksarrangor(virksomhetsnummer: String): Tiltaksarrangor

	fun getVirksomheterForAnsatt(ansattId: UUID): List<Tiltaksarrangor>
	fun getAnsatt(ansattId: UUID): Ansatt
	fun getAnsattByPersonligIdent(personIdent: String): Ansatt
	fun getTiltaksarrangorByVirksomhetsnummer(virksomhetsnummer: String): Tiltaksarrangor?
}
