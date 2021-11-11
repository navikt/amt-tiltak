package no.nav.amt.tiltak.core.port

interface ArenaOrdsProxyConnector {

	fun hentFnr(arenaPersonId: String): String?

	fun hentArbeidsgiver(arenaArbeidsgiverId: String): Arbeidsgiver?

	fun hentVirksomhetsnummer(virksomhetsnummer: String): String

}

data class Arbeidsgiver(
	val virksomhetsnummer: String,
	val organisasjonsnummerMorselskap: String
)

