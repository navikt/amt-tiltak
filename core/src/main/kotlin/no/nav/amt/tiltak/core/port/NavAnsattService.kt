package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.UpsertNavAnsattInput

interface NavAnsattService {

	fun getNavAnsatt(navIdent: String): NavAnsatt

	fun upsertNavAnsatt(input: UpsertNavAnsattInput)

}
