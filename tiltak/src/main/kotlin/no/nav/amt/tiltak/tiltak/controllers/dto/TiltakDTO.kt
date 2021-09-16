package no.nav.amt.tiltak.tiltak.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak

data class TiltakDTO(
	val tiltakskode: String,
	val tiltaksnavn: String,

	val instanser: List<TiltakInstansDTO>
) {
	companion object {
		fun create(tiltakListe: List<Tiltak>) {

		}
	}
}
