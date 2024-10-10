package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.UUID

data class DeltakerStatus(
	val id: UUID,
	val type: Type,
	val aarsak: Aarsak?,
	val aarsaksbeskrivelse: String?,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
	val aktiv: Boolean,
) {
	enum class Aarsak {
		SYK, FATT_JOBB, TRENGER_ANNEN_STOTTE, FIKK_IKKE_PLASS, IKKE_MOTT, ANNET, AVLYST_KONTRAKT, UTDANNING, SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT
	}

	enum class Type {
		UTKAST_TIL_PAMELDING, AVBRUTT_UTKAST, // nye statuser for påmelding utenfor Arena
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
	DeltakerStatus.Type.FULLFORT,
	DeltakerStatus.Type.AVBRUTT_UTKAST
)

val VENTER_PAA_PLASS_STATUSER = listOf(
	DeltakerStatus.Type.SOKT_INN,
	DeltakerStatus.Type.VURDERES,
	DeltakerStatus.Type.VENTELISTE,
	DeltakerStatus.Type.PABEGYNT_REGISTRERING,
	DeltakerStatus.Type.UTKAST_TIL_PAMELDING
)

val HAR_IKKE_STARTET_STATUSER = listOf(DeltakerStatus.Type.VENTER_PA_OPPSTART).plus(VENTER_PAA_PLASS_STATUSER)

fun DeltakerStatus.harIkkeStartet() : Boolean {
	return type in HAR_IKKE_STARTET_STATUSER
}

fun DeltakerStatus.Type.getStatustekst(): String {
	return when (this) {
		DeltakerStatus.Type.UTKAST_TIL_PAMELDING -> "Utkastet er delt og venter på godkjenning"
		DeltakerStatus.Type.AVBRUTT_UTKAST -> "Avbrutt utkast"
		DeltakerStatus.Type.VENTER_PA_OPPSTART -> "Venter på oppstart"
		DeltakerStatus.Type.DELTAR -> "Deltar"
		DeltakerStatus.Type.HAR_SLUTTET -> "Har sluttet"
		DeltakerStatus.Type.IKKE_AKTUELL -> "Ikke aktuell"
		DeltakerStatus.Type.SOKT_INN -> "Søkt om plass"
		DeltakerStatus.Type.VURDERES -> "Vurderes"
		DeltakerStatus.Type.VENTELISTE -> "På venteliste"
		DeltakerStatus.Type.AVBRUTT -> "Avbrutt"
		DeltakerStatus.Type.FULLFORT -> "Fullført"
		DeltakerStatus.Type.FEILREGISTRERT -> "Feilregistrert"
		DeltakerStatus.Type.PABEGYNT_REGISTRERING -> "Påbegynt registrering"
		DeltakerStatus.Type.PABEGYNT -> "Påbegynt registrering"
	}
}

fun DeltakerStatus.Aarsak.getVisningsnavn(beskrivelse: String?): String {
	if (beskrivelse != null) {
		return beskrivelse
	}
	return when (this) {
		DeltakerStatus.Aarsak.SYK -> "Syk"
		DeltakerStatus.Aarsak.FATT_JOBB -> "Fått jobb"
		DeltakerStatus.Aarsak.TRENGER_ANNEN_STOTTE -> "Trenger annen støtte"
		DeltakerStatus.Aarsak.FIKK_IKKE_PLASS -> "Fikk ikke plass"
		DeltakerStatus.Aarsak.IKKE_MOTT -> "Møter ikke opp"
		DeltakerStatus.Aarsak.ANNET -> "Annet"
		DeltakerStatus.Aarsak.AVLYST_KONTRAKT -> "Avlyst kontrakt"
		DeltakerStatus.Aarsak.UTDANNING -> "Utdanning"
		DeltakerStatus.Aarsak.SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT -> "Samarbeidet med arrangøren er avbrutt"
	}
}
