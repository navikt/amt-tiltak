package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.clients.nom.NomClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.Bucket
import no.nav.amt.tiltak.core.domain.nav_ansatt.UpsertNavAnsattInput
import no.nav.amt.tiltak.core.port.NavAnsattService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
internal class NavAnsattUpdater(
	private val nomClient: NomClient,
	private val navAnsattRepository: NavAnsattRepository,
	private val navAnsattService: NavAnsattService
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun oppdaterBatch() {
		navAnsattRepository.getNavAnsattInBucket(Bucket.forTidspunkt()).forEach { lagretNavAnsatt ->
			val nomNavAnsatt = nomClient.hentNavAnsatt(lagretNavAnsatt.navIdent)

			if (nomNavAnsatt == null) {
				log.warn("Fant ikke nav ansatt med ident=${lagretNavAnsatt.navIdent} i NOM")
				return@forEach
			}

			navAnsattService.upsertNavAnsatt(
				UpsertNavAnsattInput(
					id = lagretNavAnsatt.id,
					navIdent = nomNavAnsatt.navIdent,
					navn = nomNavAnsatt.navn,
					epost = nomNavAnsatt.epost,
					telefonnummer = nomNavAnsatt.telefonnummer,
				)
			)
		}
	}

}

