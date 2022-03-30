package no.nav.amt.tiltak.core.domain.veileder

import no.nav.amt.tiltak.core.domain.navansatt.AnsattTilgang
import no.nav.amt.tiltak.core.domain.navansatt.AnsattTilgang.Companion.aldriTilgang

// TODO renme til nav-ansatt?
data class Veileder(
	val navIdent: String,
	val navn: String,
	val epost: String?,
	val telefonnummer: String?,
	val enhetTilganger: AnsattTilgang = aldriTilgang
)
