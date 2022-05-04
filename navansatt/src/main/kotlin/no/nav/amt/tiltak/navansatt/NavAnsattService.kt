package no.nav.amt.tiltak.connectors.veileder

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Service

@Service
open class NavAnsattServiceImpl(
	private val veilederConnector: VeilederConnector,
) : NavAnsattService {

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		return veilederConnector.hentVeileder(navIdent)
			?: throw NoSuchElementException("Fant ikke nav ansatt med ident $navIdent")
	}

}
