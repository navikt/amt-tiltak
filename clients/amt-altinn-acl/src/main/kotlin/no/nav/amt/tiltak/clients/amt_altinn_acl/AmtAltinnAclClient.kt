package no.nav.amt.tiltak.clients.amt_altinn_acl

interface AmtAltinnAclClient {

	fun hentTiltaksarrangorRoller(norskIdent: String): List<TiltaksarrangorAnsattRoller>

}

data class TiltaksarrangorAnsattRoller(
	val organisasjonsnummer: String,
	val roller: List<TiltaksarrangorAnsattRolle>
)

enum class TiltaksarrangorAnsattRolle {
	VEILEDER, KOORDINATOR
}
