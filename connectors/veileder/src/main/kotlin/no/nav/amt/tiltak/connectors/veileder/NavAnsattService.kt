package no.nav.amt.tiltak.connectors.veileder

import no.nav.amt.tiltak.clients.axsys.AxsysClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavEnhetTilgang
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavKontorService
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class NavAnsattServiceImpl(
	private val veilederConnector: VeilederConnector,
	private val axsysClient: AxsysClient,
	private val navKontorService: NavKontorService
) : NavAnsattService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun getNavAnsatt(navIdent: String): NavAnsatt {
		return veilederConnector.hentVeileder(navIdent)
			?: throw NoSuchElementException("Fant ikke nav ansatt med ident $navIdent")
	}

	override fun hentTiltaksansvarligEnhetTilganger(navIdent: String): List<NavEnhetTilgang> {
		val tilgangerTilTiltaksadministrasjon = axsysClient.hentTilganger(navIdent)
			.filter { harTilgangTilTiltaksadministrasjon(it.temaer) }

		val enhetIder = tilgangerTilTiltaksadministrasjon
			.map { it.enhetId }

		val navKontorer = navKontorService.hentNavKontorer(enhetIder)

		return tilgangerTilTiltaksadministrasjon.map { tilgang ->
			val kontor = navKontorer.find { it.enhetId == tilgang.enhetId }

			if (kontor == null) {
				log.warn("Nav kontor fra Axsys med id ${tilgang.enhetId} finnes ikke i database")
				return@map null
			}

			return@map NavEnhetTilgang(
				kontor,
				tilgang.temaer
			)
		}.filterNotNull()
	}

	private fun harTilgangTilTiltaksadministrasjon(temaer: List<String>): Boolean {
		// Dette er ikke riktig kode, må oppdateres i fremtiden når vi har avklart hva temaet vårt skal hete
		return temaer.contains("TIL")
	}

}
