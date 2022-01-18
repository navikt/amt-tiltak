package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.veileder.Veileder

interface PersonService {

	fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon

	fun hentPerson(fnr: String): Person

	fun hentTildeltVeileder(fnr: String): Veileder?

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
)
