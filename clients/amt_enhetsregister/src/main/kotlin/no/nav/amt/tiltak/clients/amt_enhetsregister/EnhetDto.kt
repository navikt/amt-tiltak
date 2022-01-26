package no.nav.amt.tiltak.clients.amt_enhetsregister

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetDto(
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?
)
