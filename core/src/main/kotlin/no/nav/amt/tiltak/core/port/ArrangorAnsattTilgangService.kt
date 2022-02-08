package no.nav.amt.tiltak.core.port

import java.util.*

interface ArrangorAnsattTilgangService {

//	fun hentAnsattId(ansattPersonligIdent: String): UUID?
//
//	fun harTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID): Boolean
//
//	fun harTilgangTilArrangor(ansattId: UUID, arrangorId: UUID): Boolean

	fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID)


}
