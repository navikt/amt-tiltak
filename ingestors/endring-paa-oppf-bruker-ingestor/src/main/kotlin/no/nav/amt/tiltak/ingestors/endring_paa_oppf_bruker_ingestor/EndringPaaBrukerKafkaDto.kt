package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

internal class EndringPaaBrukerKafkaDto(
	val fodselsnummer: String,
	val oppfolgingsenhet: String?
)
