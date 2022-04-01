package no.nav.amt.tiltak.core.port

import java.util.*

interface ArrangorAnsattTilgangService {

	fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID)

	fun hentGjennomforingIder(ansattPersonligIdent: String): List<UUID>

	fun hentAnsattId(ansattPersonligIdent: String): UUID

}
