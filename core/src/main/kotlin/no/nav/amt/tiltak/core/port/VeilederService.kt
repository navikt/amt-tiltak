package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import java.util.*

interface VeilederService {

	fun upsertVeileder(navAnsatt: NavAnsatt): UUID

	fun getOrCreateVeileder(navIdent: String): NavAnsatt

}
