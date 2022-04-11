package no.nav.amt.tiltak.connectors.veileder

import no.nav.amt.tiltak.clients.axsys.AxsysClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavEnhetTilgang
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Service

@Service
open class NavAnsattServiceImpl(
	private val veilederConnector: VeilederConnector,
	private val axsysClient: AxsysClient
) : NavAnsattService {

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		return veilederConnector.hentVeileder(navIdent)
			?: throw NoSuchElementException("Fant ikke nav ansatt med ident $navIdent")
	}

	override fun hentEnhetTilganger(navIdent: String): List<NavEnhetTilgang> {
		return axsysClient.hentTilganger(navIdent)
			.map { NavEnhetTilgang(enhetId = it.enhetId, enhetNavn = it.navn, temaer = it.temaer)  }
	}

}
