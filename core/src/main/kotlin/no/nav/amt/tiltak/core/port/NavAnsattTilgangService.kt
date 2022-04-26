package no.nav.amt.tiltak.core.port

import java.util.*

interface NavAnsattTilgangService {

	fun harTiltaksansvarligTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID): Boolean

}
