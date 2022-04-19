package no.nav.amt.tiltak.nav_kontor

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import java.util.*

data class NavKontorDbo(
	val id: UUID,
	val enhetId: String,
	val navn: String
) {
	fun toNavKontor() = NavKontor(
		id = this.id,
		enhetId = this.enhetId,
		navn = this.navn
	)
}


