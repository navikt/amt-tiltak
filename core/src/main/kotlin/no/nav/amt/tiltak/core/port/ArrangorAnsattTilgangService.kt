package no.nav.amt.tiltak.core.port

import java.util.*

interface ArrangorAnsattTilgangService {

	fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID)

	fun verifiserTilgangTilDeltaker(ansattPersonligIdent: String, deltakerId: UUID)

	fun hentVirksomhetsnummereMedKoordinatorRettighet(ansattPersonligIdent: String): List<String>

	fun hentGjennomforingIder(ansattPersonligIdent: String): List<UUID>

	fun hentAnsattId(ansattPersonligIdent: String): UUID

	fun opprettTilgang(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun fjernTilgang(ansattPersonligIdent: String, gjennomforingId: UUID)

}
