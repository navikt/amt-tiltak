package no.nav.amt.tiltak.connectors.dkif

interface DkifConnector {

	fun hentBrukerKontaktinformasjon(fnr: String): Kontaktinformasjon

}

data class Kontaktinformasjon(
	val epost: String?,
	val telefonnummer: String?,
)
