package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.*

data class NavEnhetDbo(
	val id: UUID,
	val enhetId: String,
	val navn: String
) {
	fun toNavEnhet() = NavEnhet(
		id = this.id,
		enhetId = this.enhetId,
		navn = this.navn
	)
}


