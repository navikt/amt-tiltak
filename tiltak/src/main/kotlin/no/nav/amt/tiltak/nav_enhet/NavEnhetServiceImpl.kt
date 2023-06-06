package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
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
	private val veilarbarenaClient: VeilarbarenaClient,
	private val amtPersonClient: AmtPersonClient,
) : NavEnhetService {

	override fun getNavEnhetForBruker(personIdent: String): NavEnhet? {
		val oppfolgingsenhetId = veilarbarenaClient.hentBrukerOppfolgingsenhetId(personIdent)
			?: return null

		return getNavEnhet(oppfolgingsenhetId)
			.also {
				if (it == null) {
					secureLog.warn("Bruker med fnr=$personIdent har enhetId=$oppfolgingsenhetId som ikke finnes i norg")
				}
			}
	}

	override fun getNavEnhet(enhetId: String) =
		navEnhetRepository.hentEnhet(enhetId)?.toNavEnhet()
			?: opprettEnhet(enhetId)

	override fun getNavEnhet(id: UUID) = navEnhetRepository.get(id).toNavEnhet()

	private fun opprettEnhet(enhetId: String): NavEnhet? {
		val norgEnhet = norgClient.hentNavEnhet(enhetId) ?: return null

		val id = UUID.randomUUID()

		val insertInput = NavEnhetInsertInput(
			id = id,
			enhetId = enhetId,
			navn = norgEnhet.navn
		)

		navEnhetRepository.insert(insertInput)

		val enhet = getNavEnhet(id)
		amtPersonClient.migrerNavEnhet(enhet)
		return enhet
	}

}
