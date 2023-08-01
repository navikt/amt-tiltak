package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import java.util.UUID

data class NavAnsattInput(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val telefonnummer: String,
	val epost: String
) {
	fun toModel() = NavAnsatt(id, navIdent, navn, epost, telefonnummer)
}
