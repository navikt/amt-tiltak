package no.nav.amt.tiltak.core.port

import java.util.*

interface TiltaksansvarligAutoriseringService {

	fun verifiserTilgangTilFlate(navIdent: String)

	fun verifiserTilgangTilEndringsmelding(navIdent: String)

	fun verifiserTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID)

}
