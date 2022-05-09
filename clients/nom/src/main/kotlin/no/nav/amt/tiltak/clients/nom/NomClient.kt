package no.nav.amt.tiltak.clients.nom

interface NomClient {

	fun hentNavAnsatt(navIdent: String): NomNavAnsatt?

}
