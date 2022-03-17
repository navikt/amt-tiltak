package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import java.util.*

data class NavKontorDbo(
	val id: UUID,
	val enhetId: String,
	val navn: String
) {
	fun toNavKontor() = NavKontor(
		enhetId = this.enhetId,
		navn = this.navn
	)
}


