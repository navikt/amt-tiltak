package no.nav.amt.tiltak.core.domain.navansatt

data class NavAnsatt(
	val navIdent: String,
	val navn: String,
	val tilganger: AnsattTilgang
) {
	fun tilgang(enhet: String, tema: String) {

	}
}
