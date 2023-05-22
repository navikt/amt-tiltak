package no.nav.amt.tiltak.clients.amt_person

import no.nav.amt.tiltak.clients.amt_person.dto.OpprettArrangorAnsattDto
import no.nav.amt.tiltak.clients.amt_person.dto.OpprettNavBrukerDto
import no.nav.amt.tiltak.clients.amt_person.model.NavBruker
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet

interface AmtPersonClient {

	fun hentNavBruker(personIdent: String): Result<NavBruker>
	fun hentNavAnsatt(navIdent: String): Result<NavAnsatt>
	fun hentNavEnhet(enhetId: String): Result<NavEnhet>

	fun migrerNavBruker(navBrukerDto: OpprettNavBrukerDto)
	fun migrerNavAnsatt(navAnsatt: NavAnsatt)
	fun migrerNavEnhet(navEnhet: NavEnhet)
	fun migrerArrangorAnsatt(arrangorAnsattDto: OpprettArrangorAnsattDto)

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
