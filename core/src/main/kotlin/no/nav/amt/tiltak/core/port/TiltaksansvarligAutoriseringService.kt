package no.nav.amt.tiltak.core.port

import java.util.*

interface TiltaksansvarligAutoriseringService {

	fun verifiserTilgangTilFlate(navAnsattAzureId: UUID)

	fun verifiserTilgangTilEndringsmelding(navAnsattAzureId: UUID)

	fun verifiserTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID)

}
