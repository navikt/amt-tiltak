package no.nav.amt.tiltak.connectors.veilarboppfolging

interface VeilarboppfolgingClient {

	fun hentVeilederIdent(fnr: String) : String?

}
