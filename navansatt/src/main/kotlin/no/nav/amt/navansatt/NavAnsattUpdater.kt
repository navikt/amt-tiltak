package no.nav.amt.navansatt

import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Component

@Component
internal class NavAnsattUpdater(
	private val veilederConnector: VeilederConnector,
	private val veilederService: VeilederServiceImpl // TODO burde vi ha separat interface for batchupdate - for det er sannsynligvis ikke core. Det er derfor impl tas inn som bønne
) {

	fun oppdaterBatch() {
		veilederService.getVeilederBatch(NavAnsattBucket.forCurrentTime()).forEach { dbVeileder ->
			veilederConnector.hentVeileder(dbVeileder.navIdent)?.let { nomVeileder ->
				// TODO maybe implement some change detection
				veilederService.upsertVeileder(nomVeileder)
			}
		}
	}

}

