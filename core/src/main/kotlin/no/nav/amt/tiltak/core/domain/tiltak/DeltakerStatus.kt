package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.*

data class DeltakerStatus(
	val id: UUID,
	val type: Deltaker.Status,
	val aarsak: Aarsak?,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
	val aktiv: Boolean,
) {
	data class Aarsak(
		val type: Deltaker.StatusAarsak,
		val beskrivelse: String? = null,
	) {
		init {
		    if (beskrivelse != null && type != Deltaker.StatusAarsak.ANNET) {
				throw IllegalStateException("Aarsak $type skal ikke ha beskrivelse")
			}
		}
	}
}



