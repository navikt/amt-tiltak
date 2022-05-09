package no.nav.amt.tiltak.core.port

interface PersonService {

	fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon

	fun hentPerson(fnr: String): Person

	fun hentTildeltVeilederNavIdent(fnr: String): String?

	fun hentGjeldendePersonligIdent(ident: String): String

}

data class Kontaktinformasjon(
	val epost: String?,
	val telefonnummer: String?,
)

data class Person(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val diskresjonskode: Diskresjonskode?
)

enum class Diskresjonskode {
	KODE_6, // STRENGT_FORTROLIG
	KODE_7, // FORTROLIG
	KODE_19, // STRENGT_FORTROLIG_UTLAND
}
