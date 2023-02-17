package no.nav.amt.tiltak.kafka.virksomheter_ingestor

data class VirksomhetDto (
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
)
