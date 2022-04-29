package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.springframework.stereotype.Service
import java.util.*

@Service
open class NavEnhetServiceImpl(
	private val navEnhetRepository: NavEnhetRepository,
	private val veilarbarenaClient: VeilarbarenaClient
) : NavEnhetService {

	override fun hentNavEnheter(enhetIder: List<String>): List<NavEnhet> {
		return navEnhetRepository.hentEnheter(enhetIder)
			.map { NavEnhet(id = it.id, enhetId = it.enhetId, navn = it.navn) }
	}

	override fun upsertNavEnhet(enhetId: String, navn: String) {
		navEnhetRepository.upsert(enhetId, navn)
	}

	override fun getNavEnhetForBruker(fodselsnummer: String): NavEnhet? {
		return veilarbarenaClient.hentBrukerOppfolgingsenhetId(fodselsnummer)?.let {
			navEnhetRepository.hentEnhet(it) ?: throw IllegalStateException("Ugyldig kontor for bruker ($it")
		}?.toNavEnhet()
	}

	override fun getNavEnhet(enhetId: String) = navEnhetRepository.hentEnhet(enhetId)?.toNavEnhet()
		?: throw IllegalStateException("Ugyldig kontor $enhetId")

	override fun getNavEnhet(id: UUID) = navEnhetRepository.get(id).toNavEnhet()

}
