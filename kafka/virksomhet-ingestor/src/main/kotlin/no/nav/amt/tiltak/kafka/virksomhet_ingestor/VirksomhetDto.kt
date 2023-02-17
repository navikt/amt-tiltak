package no.nav.amt.tiltak.kafka.virksomhet_ingestor

data class VirksomhetDto (
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
)
