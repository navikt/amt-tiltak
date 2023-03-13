package no.nav.amt.tiltak.core.port

import java.util.*

interface MineDeltakerlisterService {

	fun leggTil(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID)
	fun fjern(arrangorAnsattId: UUID, gjennomforingId: UUID)
	fun fjernAlleHosArrangor(arrangorAnsattId: UUID, arrangorId: UUID)
	fun hent(ansattId: UUID): List<UUID>
	fun erLagtTil(ansattId: UUID, gjennomforingId: UUID): Boolean
}
