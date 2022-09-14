package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet

data class NavEnhetDto(
	val navn: String,
)

fun NavEnhet.toDto() = NavEnhetDto(
	navn = navn,
)
