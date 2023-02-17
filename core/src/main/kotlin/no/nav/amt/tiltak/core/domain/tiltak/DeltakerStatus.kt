package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.*

data class DeltakerStatus(
	val id: UUID,
	val type: Type,
	val aarsak: Aarsak?,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
	val aktiv: Boolean,
) {
	data class Aarsak(
		val type: Type,
		val beskrivelse: String? = null,
	) {
		init {
		    if (beskrivelse != null && type != Type.ANNET) {
				throw IllegalStateException("Aarsak $type skal ikke ha beskrivelse")
			}
		}

		enum class Type {
			SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, UTDANNING, FERDIG, AVLYST_KONTRAKT, IKKE_MOTT, FEILREGISTRERT, ANNET
		}
	}

	enum class Type {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT, PABEGYNT_REGISTRERING, PABEGYNT
		//PABEGYNT er erstattet av PABEGYNT_REGISTRERING, men må beholdes så lenge statusen er på topicen
	}

}



