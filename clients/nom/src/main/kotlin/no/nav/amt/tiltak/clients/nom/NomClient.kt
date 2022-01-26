package no.nav.amt.tiltak.clients.nom

interface NomClient {

	fun hentVeileder(navIdent: String): NomVeileder?

}
