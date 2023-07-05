package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import java.time.LocalDateTime
import java.util.UUID

interface ArrangorAnsattService {

	fun upsertAnsatt(arrangorAnsatt: ArrangorAnsatt)

	fun getAnsatt(ansattId: UUID): Ansatt

 	fun getAnsattByPersonligIdent(personIdent: String): Ansatt?

	fun setTilgangerSistSynkronisert(ansattId: UUID, sistOppdatert: LocalDateTime)
	fun getAnsatteSistSynkronisertEldreEnn(eldreEnn: LocalDateTime, maksAntall: Int): List<Ansatt>
}
