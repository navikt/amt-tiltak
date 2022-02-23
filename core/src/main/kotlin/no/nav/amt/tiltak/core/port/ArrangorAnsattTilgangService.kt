package no.nav.amt.tiltak.core.port

import java.util.*

interface ArrangorAnsattTilgangService {

	fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID)

	fun hentGjennomforingIderForAnsattHosArrangor(ansattPersonligIdent: String, arrangorId: UUID): List<UUID>

}
