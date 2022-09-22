package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt

data class NavVeilederDto(
	val navn: String,
	val telefon: String?,
	val epost: String?,
)

fun NavAnsatt.toDto() = NavVeilederDto(
	navn = navn,
	telefon = telefonnummer,
	epost = epost,
)
