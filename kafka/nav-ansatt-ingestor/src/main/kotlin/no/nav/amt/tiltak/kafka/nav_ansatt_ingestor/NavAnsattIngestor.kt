package no.nav.amt.tiltak.kafka.nav_ansatt_ingestor

import NavAnsattDto
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.kafka.NavAnsattIngestor
import no.nav.amt.tiltak.core.port.NavAnsattService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class NavAnsattIngestorImpl(
	private val navAnsattService: NavAnsattService,
): NavAnsattIngestor {
	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingest(value: String) {
		val ansatt = fromJsonString<NavAnsattDto>(value)

		navAnsattService.upsert(NavAnsatt(
			id = ansatt.id,
			navIdent = ansatt.navident,
			navn = ansatt.navn,
			telefonnummer = ansatt.telefon,
			epost = ansatt.epost
		))

		log.info("Håndterte melding for nav-ansatt ${ansatt.id}")
	}
}
