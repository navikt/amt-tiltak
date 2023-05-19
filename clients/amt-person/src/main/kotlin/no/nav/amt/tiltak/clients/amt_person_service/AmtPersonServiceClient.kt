package no.nav.amt.tiltak.clients.amt_person_service

import no.nav.amt.tiltak.clients.amt_person_service.model.NavBruker
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet

interface AmtPersonServiceClient {

	fun hentNavBruker(personIdent: String): Result<NavBruker>

	fun hentNavAnsatt(navIdent: String): Result<NavAnsatt>

	fun hentNavEnhet(enhetId: String): Result<NavEnhet>

}

data class NavBrukerRequest(
	val personIdent: String
)

data class NavAnsattRequest(
	val navIdent: String
)

data class NavEnhetRequest(
	val enhetId: String
)
