package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerPayload(
	val id: UUID,
	val gjennomforingId: UUID,
	val personIdent: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Status,
	val statusAarsak: StatusAarsak?,
	val dagerPerUke: Float?,
	val prosentDeltid: Float?,
	val registrertDato: LocalDateTime,
	val statusEndretDato: LocalDateTime?,
	val innsokBegrunnelse: String?
) {
	enum class Status {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT, PABEGYNT, PABEGYNT_REGISTRERING,
		SOKT_INN, VURDERES, VENTELISTE, AVBRUTT, FULLFORT // kurs statuser
		//PABEGYNT er erstattet av PABEGYNT_REGISTRERING, men må beholdes så lenge statusen er på topicen

	}

	enum class StatusAarsak {
		SYK,
		FATT_JOBB,
		TRENGER_ANNEN_STOTTE,
		FIKK_IKKE_PLASS,
		AVLYST_KONTRAKT,
		IKKE_MOTT,
		ANNET
	}
}
