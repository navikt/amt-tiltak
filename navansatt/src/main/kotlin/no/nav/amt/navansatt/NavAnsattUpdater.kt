package no.nav.amt.navansatt

import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Component

@Component
internal class NavAnsattUpdater(
	private val veilederConnector: VeilederConnector,
	private val veilederService: VeilederServiceImpl
) {

	fun oppdaterBatch() {
		veilederService.getVeilederBatch(Bucket.forTidspunkt()).forEach { dbVeileder ->
			veilederConnector.hentVeileder(dbVeileder.navIdent)?.let { nomVeileder ->
				veilederService.upsertVeileder(nomVeileder)
			}
		}
	}

}

