package no.nav.amt.tiltak.core.port

interface PersonService {

	fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon

	fun hentPerson(fnr: String): Person

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
)
