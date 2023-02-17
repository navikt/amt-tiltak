package no.nav.amt.tiltak.core.domain.arrangor


data class ArrangorUpdate(
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
)
