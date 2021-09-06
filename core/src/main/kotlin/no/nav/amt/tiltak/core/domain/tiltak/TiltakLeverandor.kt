package no.nav.amt.tiltak.core.domain.tiltak

data class TiltakLeverandor(
	val organisasjonsnummer: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
	val tiltakInstanser: List<TiltakInstans>
)
