package no.nav.amt.tiltak.connectors.nom.client

interface NomClient {

	fun hentVeileder(navIdent: String): NomVeileder?

}
