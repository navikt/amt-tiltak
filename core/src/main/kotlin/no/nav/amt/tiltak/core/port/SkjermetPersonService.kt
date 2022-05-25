package no.nav.amt.tiltak.core.port

interface SkjermetPersonService {

	fun erSkjermet(norskIdent: String): Boolean

}
