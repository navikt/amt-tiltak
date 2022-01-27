package no.nav.amt.tiltak.clients.veilarboppfolging

interface VeilarboppfolgingClient {

	fun hentVeilederIdent(fnr: String) : String?

}
