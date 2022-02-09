package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerPayload(
	val id: UUID,
	val gjennomforingId: UUID,
	val personIdent: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Status,
	val dagerPerUke: Int?,
	val prosentDeltid: Float?,
	val registrertDato: LocalDateTime,
	val statusEndretDato: LocalDateTime
) {
	enum class Status {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT
	}
}
