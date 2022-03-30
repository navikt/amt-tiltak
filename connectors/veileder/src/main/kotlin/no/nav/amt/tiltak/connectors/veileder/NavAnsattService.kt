package no.nav.amt.tiltak.navansatt.auth

import no.nav.amt.tiltak.clients.axsys.AxsysClient
import no.nav.amt.tiltak.clients.axsys.Enhet
import no.nav.amt.tiltak.clients.axsys.Enheter
import no.nav.amt.tiltak.core.domain.navansatt.AnsattTilgang
import no.nav.amt.tiltak.core.domain.navansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Service

@Service
open class NavAnsattServiceImpl(
	private val veilederConnector: VeilederConnector,
	private val axsysClient: AxsysClient
) : NavAnsattService {

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		val veileder =
			requireNotNull(veilederConnector.hentVeileder(navIdent)) { "Fant ikke nav-ansatt" }  // TODO cache?


		return NavAnsatt(
			navIdent = veileder.navIdent,
			navn = veileder.navn,
			tilganger = LazyAnsattTilgang { axsysClient.hentTilganger(navIdent) }
		)

	}

}

private class LazyAnsattTilgang(
	private val tilgangSupplier: () -> Enheter
) : AnsattTilgang {

	private val enheter by lazy { tilgangSupplier() }

	override fun harTilgang(enhetId: String, tema: String): Boolean {
		return enheter.finnMedId(enhetId)
			?.temaer
			?.any { it == tema }
			?: false
	}

	fun Enheter.finnMedId(enhetId: String) : Enhet? =
		enheter.firstOrNull { it.enhetId == enhetId }

}
