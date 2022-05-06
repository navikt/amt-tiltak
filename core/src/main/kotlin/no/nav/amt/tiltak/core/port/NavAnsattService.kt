package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import java.util.*

interface NavAnsattService {

	fun getNavAnsatt(navIdent: String): NavAnsatt

	fun upsertVeileder(navAnsatt: NavAnsatt): UUID

	fun getOrCreateNavAnsatt(navIdent: String): NavAnsatt

}
