package no.nav.amt.tiltak.nav_kontor

import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.port.NavKontorService
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.util.*

@Service
open class NavKontorServiceImpl(
	private val navKontorRepository: NavKontorRepository,
	private val veilarbarenaClient: VeilarbarenaClient
) : NavKontorService {

	override fun hentNavKontorer(enhetIder: List<String>): List<NavKontor> {
		return navKontorRepository.hentEnheter(enhetIder)
			.map { NavKontor(id = it.id, enhetId = it.enhetId, navn = it.navn) }
	}

	override fun getNavKontorForBruker(fodselsnummer: String): NavKontor? {
		return veilarbarenaClient.hentBrukerOppfolgingsenhetId(fodselsnummer)?.let {
			navKontorRepository.hentEnhet(it) ?: throw IllegalStateException("Ugyldig kontor for bruker ($it")
		}?.toNavKontor()
	}

	override fun getNavKontor(enhetId: String) = navKontorRepository.hentEnhet(enhetId)?.toNavKontor()
		?: throw IllegalStateException("Ugyldig kontor ($enhetId")

	override fun getNavKontor(id: UUID) = navKontorRepository.get(id).toNavKontor()

}
