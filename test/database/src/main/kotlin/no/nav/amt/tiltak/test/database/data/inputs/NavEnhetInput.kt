package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.*

data class NavEnhetInput(
	val id: UUID,
	val enhetId: String,
	val navn: String
) {
	fun toNavEnhet(): NavEnhet {
		return NavEnhet(id, enhetId, navn)
	}
}

