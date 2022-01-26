package no.nav.amt.tiltak.clients.dkif

interface DkifClient {

	fun hentBrukerKontaktinformasjon(fnr: String): Kontaktinformasjon

}

data class Kontaktinformasjon(
	val epost: String?,
	val telefonnummer: String?,
)
