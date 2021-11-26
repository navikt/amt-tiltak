package no.nav.amt.tiltak.tiltaksleverandor.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.TilknyttetLeverandor
import java.util.*

data class AnsattDTO(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val leverandorer: List<TilknyttetLeverandorDTO>
)

data class TilknyttetLeverandorDTO(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val roller: List<String>
)

fun Ansatt.toDto(): AnsattDTO {
	return AnsattDTO(
		id = this.id,
		personligIdent = this.personligIdent,
		fornavn = this.fornavn,
		etternavn = this.etternavn,
		telefonnummer = this.telefonnummer,
		epost = this.epost,
		leverandorer = this.leverandorer.map { it.toDto() }
	)
}

fun TilknyttetLeverandor.toDto(): TilknyttetLeverandorDTO {
	return TilknyttetLeverandorDTO(
		id = this.id,
		navn = this.navn,
		organisasjonsnummer = this.organisasjonsnummer,
		overordnetEnhetOrganisasjonsnummer = this.overordnetEnhetOrganisasjonsnummer,
		overordnetEnhetNavn = this.overordnetEnhetNavn,
		roller = this.roller
	)
}
