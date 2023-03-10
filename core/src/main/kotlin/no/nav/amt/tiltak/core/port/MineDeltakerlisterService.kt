package no.nav.amt.tiltak.core.port

import java.util.*

interface MineDeltakerlisterService {

	fun leggTil(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID)
	fun fjern(arrangorAnsattId: UUID, gjennomforingId: UUID)
	fun fjernGjennomforinger(arrangorAnsattId: UUID, arrangorId: UUID)
	fun hentAlleForAnsatt(ansattId: UUID): List<UUID>
	fun harLagtTilDeltakerliste(ansattId: UUID, gjennomforingId: UUID): Boolean
}
