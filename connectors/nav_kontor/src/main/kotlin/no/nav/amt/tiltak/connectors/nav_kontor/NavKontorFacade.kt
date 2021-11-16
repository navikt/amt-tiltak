package no.nav.amt.tiltak.connectors.nav_kontor

import no.nav.amt.tiltak.connectors.norg.NorgConnector
import no.nav.amt.tiltak.connectors.veilarbarena.VeilarbarenaConnector
import no.nav.amt.tiltak.core.port.NavKontor
import no.nav.amt.tiltak.core.port.NavKontorService
import org.springframework.stereotype.Service

@Service
class NavKontorFacade(
	private val veilarbarenaConnector: VeilarbarenaConnector,
	private val norgConnector: NorgConnector
) : NavKontorService {

	override fun hentNavKontorForBruker(fnr: String): NavKontor? {
		val oppfolgingsenhetId = veilarbarenaConnector.hentBrukerOppfolgingsenhetId(fnr) ?: return null

		val kontorNavn = norgConnector.hentNavKontorNavn(oppfolgingsenhetId)

		return NavKontor(
			enhetId = oppfolgingsenhetId,
			navn = kontorNavn
		)
	}

}
