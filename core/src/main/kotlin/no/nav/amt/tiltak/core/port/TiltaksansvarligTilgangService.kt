package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tilgangskontroll.TiltaksansvarligGjennomforingTilgang
import java.util.*

interface TiltaksansvarligTilgangService {

	fun harTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID): Boolean

	fun giTilgangTilGjennomforing(navAnsattId: UUID, gjennomforingId: UUID)

	fun fjernTilgangTilGjennomforing(navAnsattId: UUID, gjennomforingId: UUID)

	fun hentAktiveTilganger(navAnsattId: UUID): List<TiltaksansvarligGjennomforingTilgang>

}
