package no.nav.amt.tiltak.core.port

interface VeilarboppfolgingConnector {
	fun hentVeilederIdent(fnr: String) : String?
}
