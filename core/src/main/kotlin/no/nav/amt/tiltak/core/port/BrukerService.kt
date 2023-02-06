package no.nav.amt.tiltak.core.port

interface BrukerService {
	fun update(brukerIdent: String, fornavn: String, mellomnavn: String?, etternavn: String)
}
