package no.nav.amt.tiltak.test.database.data.commands

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import java.util.*

data class InsertArrangorCommand(
	val id: UUID,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val organisasjonsnummer: String,
	val navn: String
) {
	fun toArrangor() : Arrangor {
		return Arrangor(
			id = this.id,
			navn = this.navn,
			organisasjonsnummer = this.organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = this.overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = this.overordnetEnhetNavn
		)
	}

}
