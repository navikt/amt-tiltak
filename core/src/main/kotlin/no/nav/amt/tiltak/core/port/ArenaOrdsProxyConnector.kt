package no.nav.amt.tiltak.core.port

interface ArenaOrdsProxyConnector {

	fun hentFnr(arenaPersonId: String): String?

	fun hentArbeidsgiver(arenaArbeidsgiverId: String): Arbeidsgiver?

}

data class Arbeidsgiver(
	val virksomhetsnummer: String,
	val organisasjonsnummerMorselskap: String
)

