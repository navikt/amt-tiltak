package no.nav.amt.tiltak.connectors.nav_kontor

import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.port.NavKontorConnector
import org.springframework.stereotype.Service
import java.util.*

@Service
open class NavKontorConnectorImpl(
	private val veilarbarenaClient: VeilarbarenaClient,
	private val norgClient: NorgClient
) : NavKontorConnector {

	override fun hentNavKontorForBruker(fnr: String): NavKontor? {
		val oppfolgingsenhetId = veilarbarenaClient.hentBrukerOppfolgingsenhetId(fnr) ?: return null

		val kontorNavn = norgClient.hentNavKontorNavn(oppfolgingsenhetId)

		return NavKontor(
			id = UUID.randomUUID(), // Problemet her er at vi bruker domeneobjekter for data som vi mottar fra utsiden
			enhetId = oppfolgingsenhetId,
			navn = kontorNavn
		)
	}

}
