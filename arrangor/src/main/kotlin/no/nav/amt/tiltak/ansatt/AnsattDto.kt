package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import java.util.*

data class AnsattDto(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val etternavn: String,
	val arrangorer: List<TilknyttetArrangorDto>
)

data class TilknyttetArrangorDto(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val roller: List<String>
)

fun Ansatt.toDto(): AnsattDto {
	return AnsattDto(
		id = this.id,
		personligIdent = this.personligIdent,
		fornavn = this.fornavn,
		etternavn = this.etternavn,
		arrangorer = this.arrangorer.map { it.toDto() }
	)
}

fun TilknyttetArrangor.toDto(): TilknyttetArrangorDto {
	return TilknyttetArrangorDto(
		id = this.id,
		navn = this.navn,
		organisasjonsnummer = this.organisasjonsnummer,
		overordnetEnhetOrganisasjonsnummer = this.overordnetEnhetOrganisasjonsnummer,
		overordnetEnhetNavn = this.overordnetEnhetNavn,
		roller = this.roller
	)
}
