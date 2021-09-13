package no.nav.amt.tiltak.tiltak.controllers.dto

import java.util.*

data class TiltakDTO(
	val id: UUID,
	val navn: String, //sveisekurs
	val type: String, //GRUPPE_AMO
	val typeNavn: String, //Gruppe AMO
	val instanser: List<TiltakInstansDTO>
)
