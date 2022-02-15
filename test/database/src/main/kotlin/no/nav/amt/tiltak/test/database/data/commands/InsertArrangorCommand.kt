package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertArrangorCommand(
	val id: UUID,
	val overordnet_enhet_organisasjonsnummer: String?,
	val overordnet_enhet_navn: String?,
	val organisasjonsnummer: String,
	val navn: String
)
