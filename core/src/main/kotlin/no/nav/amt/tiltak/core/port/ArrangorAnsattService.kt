package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import java.time.LocalDateTime
import java.util.UUID

interface ArrangorAnsattService {

	fun upsertAnsatt(arrangorAnsatt: ArrangorAnsatt)

	fun getAnsatt(ansattId: UUID): Ansatt

	fun getAnsatte(ansattIder: List<UUID>): List<Ansatt>

 	fun getAnsattByPersonligIdent(personIdent: String): Ansatt?


	fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): List<Ansatt>

	fun getVeiledereForArrangor(arrangorId: UUID) : List<Ansatt>

	fun setTilgangerSistSynkronisert(ansattId: UUID, sistOppdatert: LocalDateTime)
	fun getAnsatteSistSynkronisertEldreEnn(eldreEnn: LocalDateTime, maksAntall: Int): List<Ansatt>
	fun setVellykketInnlogging(ansattId: UUID)
	fun getAnsattIdByPersonligIdent(personIdent: String): UUID
}
