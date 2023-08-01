package no.nav.amt.tiltak.clients.amt_person

import no.nav.amt.tiltak.clients.amt_person.model.AdressebeskyttelseGradering
import no.nav.amt.tiltak.clients.amt_person.model.NavBruker
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet

interface AmtPersonClient {

	fun hentNavBruker(personident: String): Result<NavBruker>
	fun hentNavAnsatt(navIdent: String): Result<NavAnsatt>
	fun hentNavEnhet(enhetId: String): Result<NavEnhet>
	fun hentAdressebeskyttelse(personident: String): Result<AdressebeskyttelseGradering?>

}

data class PersonRequest(
	val personident: String
)

data class NavAnsattRequest(
	val navIdent: String
)

data class NavEnhetRequest(
	val enhetId: String
)
