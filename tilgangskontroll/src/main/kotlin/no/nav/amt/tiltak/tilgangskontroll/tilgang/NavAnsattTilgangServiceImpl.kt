package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavAnsattTilgangService
import org.springframework.stereotype.Service
import java.util.*

@Service
open class NavAnsattTilgangServiceImpl(
	private val navAnsattService: NavAnsattService,
	private val gjennomforingService: GjennomforingService,
) : NavAnsattTilgangService {

	override fun harTiltaksansvarligTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID): Boolean {
		val enhetTilganger = navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent)

		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

		return gjennomforing.navEnhetId != null && enhetTilganger.any { it.enhet.id == gjennomforing.navEnhetId }
	}

}
