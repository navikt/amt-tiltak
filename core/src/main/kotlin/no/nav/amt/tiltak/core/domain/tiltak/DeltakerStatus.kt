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
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT, PABEGYNT_REGISTRERING, PABEGYNT,
		SOKT_INN, VURDERES, VENTELISTE, AVBRUTT // kurs statuser
		//PABEGYNT er erstattet av PABEGYNT_REGISTRERING, men må beholdes så lenge statusen er på topicen
	}

}

val AVSLUTTENDE_STATUSER = listOf(
	DeltakerStatus.Type.HAR_SLUTTET,
	DeltakerStatus.Type.IKKE_AKTUELL,
	DeltakerStatus.Type.FEILREGISTRERT,
	DeltakerStatus.Type.AVBRUTT
)

val HAR_IKKE_STARTET_STATUSER = listOf(
	DeltakerStatus.Type.VENTER_PA_OPPSTART,
	DeltakerStatus.Type.SOKT_INN,
	DeltakerStatus.Type.VURDERES,
	DeltakerStatus.Type.VENTELISTE,
	DeltakerStatus.Type.PABEGYNT_REGISTRERING
)
fun DeltakerStatus.erAvsluttende() : Boolean {
	return type in AVSLUTTENDE_STATUSER
}

fun DeltakerStatus.harIkkeStartet() : Boolean {
	return type in HAR_IKKE_STARTET_STATUSER
}

