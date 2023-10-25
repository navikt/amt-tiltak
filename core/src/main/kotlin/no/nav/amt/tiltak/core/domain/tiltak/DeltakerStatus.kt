package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.UUID

data class DeltakerStatus(
	val id: UUID,
	val type: Type,
	val aarsak: Aarsak?,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
	val aktiv: Boolean,
) {
	enum class Aarsak {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, IKKE_MOTT, ANNET, AVLYST_KONTRAKT
	}

	enum class Type {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT,
		SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, FULLFORT, // kurs statuser
		PABEGYNT_REGISTRERING, PABEGYNT, //PABEGYNT er erstattet av PABEGYNT_REGISTRERING, men må beholdes så lenge statusen er på topicen
	}

}

val AVSLUTTENDE_STATUSER = listOf(
	DeltakerStatus.Type.HAR_SLUTTET,
	DeltakerStatus.Type.IKKE_AKTUELL,
	DeltakerStatus.Type.FEILREGISTRERT,
	DeltakerStatus.Type.AVBRUTT,
	DeltakerStatus.Type.FULLFORT
)

val VENTER_PAA_PLASS_STATUSER = listOf(
	DeltakerStatus.Type.SOKT_INN,
	DeltakerStatus.Type.VURDERES,
	DeltakerStatus.Type.VENTELISTE,
	DeltakerStatus.Type.PABEGYNT_REGISTRERING
)

val STATUSER_SOM_KAN_SKJULES = listOf(
	DeltakerStatus.Type.IKKE_AKTUELL,
	DeltakerStatus.Type.HAR_SLUTTET,
	DeltakerStatus.Type.AVBRUTT,
	DeltakerStatus.Type.FULLFORT
)

val HAR_IKKE_STARTET_STATUSER = listOf(DeltakerStatus.Type.VENTER_PA_OPPSTART).plus(VENTER_PAA_PLASS_STATUSER)

fun DeltakerStatus.harIkkeStartet() : Boolean {
	return type in HAR_IKKE_STARTET_STATUSER
}

