package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import java.time.LocalDateTime
import java.util.*

interface ArrangorAnsattService {

	fun opprettAnsattHvisIkkeFinnes(personIdent: String): Ansatt

	fun getAnsatt(ansattId: UUID): Ansatt

 	fun getAnsattByPersonligIdent(personIdent: String): Ansatt?

	fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): List<Ansatt>

	fun setSistOppdatertForAnsatt(personIdent: String, sistOppdatert: LocalDateTime)
	fun getEldsteSistOppdaterteAnsattIds(antall: Int): List<String>
}
