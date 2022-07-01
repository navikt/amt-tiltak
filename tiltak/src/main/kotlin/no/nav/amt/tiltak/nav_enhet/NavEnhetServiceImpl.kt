package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import org.springframework.stereotype.Service
import java.util.*

@Service
open class NavEnhetServiceImpl(
	private val norgClient: NorgClient,
	private val navEnhetRepository: NavEnhetRepository,
	private val veilarbarenaClient: VeilarbarenaClient
) : NavEnhetService {

	override fun getNavEnhetForBruker(fodselsnummer: String): NavEnhet? {
		val oppfolgingsenhetId = veilarbarenaClient.hentBrukerOppfolgingsenhetId(fodselsnummer)
			?: return null

		return getNavEnhet(oppfolgingsenhetId)
			.also {
				if (it == null) {
					secureLog.warn("Bruker med fnr=$fodselsnummer har enhetId=$oppfolgingsenhetId som ikke finnes i norg")
				}
			}
	}

	override fun getNavEnhet(enhetId: String): NavEnhet? {
		val enhet = navEnhetRepository.hentEnhet(enhetId)?.toNavEnhet()

		if (enhet != null) {
			return enhet
		}

		return opprettEnhet(enhetId)
	}

	override fun getNavEnhet(id: UUID) = navEnhetRepository.get(id).toNavEnhet()

	private fun opprettEnhet(enhetId: String): NavEnhet {
		val enhetNavn = norgClient.hentNavEnhetNavn(enhetId)

		val id = UUID.randomUUID()

		val insertInput = NavEnhetInsertInput(
			id = id,
			enhetId = enhetId,
			navn = enhetNavn
		)

		navEnhetRepository.insert(insertInput)

		return getNavEnhet(id)
	}

}
