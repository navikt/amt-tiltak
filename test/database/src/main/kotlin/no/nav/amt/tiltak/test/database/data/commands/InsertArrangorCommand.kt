package no.nav.amt.tiltak.test.database.data.commands

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import java.util.*

data class InsertArrangorCommand(
	val id: UUID,
	val overordnet_enhet_organisasjonsnummer: String?,
	val overordnet_enhet_navn: String?,
	val organisasjonsnummer: String,
	val navn: String
) {
	fun toArrangor() : Arrangor {
		return Arrangor(
			id = this.id,
			navn = this.navn,
			organisasjonsnummer = this.organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = this.overordnet_enhet_organisasjonsnummer,
			overordnetEnhetNavn = this.overordnet_enhet_navn
		)
	}

}
