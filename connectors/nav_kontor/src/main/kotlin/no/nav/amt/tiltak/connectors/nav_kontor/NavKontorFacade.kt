package no.nav.amt.tiltak.connectors.nav_kontor

import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import no.nav.amt.tiltak.core.port.NavKontor
import no.nav.amt.tiltak.core.port.NavKontorService
import org.springframework.stereotype.Service

@Service
class NavKontorFacade(
	private val veilarbarenaConnector: VeilarbarenaClient,
	private val norgClient: NorgClient
) : NavKontorService {

	override fun hentNavKontorForBruker(fnr: String): NavKontor? {
		val oppfolgingsenhetId = veilarbarenaConnector.hentBrukerOppfolgingsenhetId(fnr) ?: return null

		val kontorNavn = norgClient.hentNavKontorNavn(oppfolgingsenhetId)

		return NavKontor(
			enhetId = oppfolgingsenhetId,
			navn = kontorNavn
		)
	}

}
