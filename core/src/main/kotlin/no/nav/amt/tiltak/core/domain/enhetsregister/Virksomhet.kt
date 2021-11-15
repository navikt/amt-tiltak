package no.nav.amt.tiltak.core.domain.enhetsregister

data class Virksomhet(
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String? = null,
	val overordnetEnhetNavn: String? = null,
)
