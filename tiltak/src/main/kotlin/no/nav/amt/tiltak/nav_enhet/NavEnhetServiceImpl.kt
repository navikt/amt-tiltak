package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
open class NavEnhetServiceImpl(
	private val navEnhetRepository: NavEnhetRepository,
	private val amtPersonClient: AmtPersonClient,
) : NavEnhetService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun getNavEnhet(enhetId: String) =
		navEnhetRepository.hentEnhet(enhetId)?.toNavEnhet()
			?: opprettEnhet(enhetId)

	override fun getNavEnhet(id: UUID) = navEnhetRepository.get(id).toNavEnhet()

	override fun upsert(enhet: NavEnhet) {
		val eksisterendeEnhet = navEnhetRepository.hentEnhet(enhet.enhetId)?.toNavEnhet()

		if (eksisterendeEnhet != enhet) {
			navEnhetRepository.upsert(enhet)
			log.info("Upsertet nav-enhet ${enhet.id}")
		}

	}

	private fun opprettEnhet(enhetId: String): NavEnhet? {
		val navEnhet = amtPersonClient.hentNavEnhet(enhetId)
			.recover {
				if (it is NoSuchElementException) return null
				else throw it
			}.getOrThrow()

		navEnhetRepository.upsert(navEnhet)

		return navEnhet
	}

}
