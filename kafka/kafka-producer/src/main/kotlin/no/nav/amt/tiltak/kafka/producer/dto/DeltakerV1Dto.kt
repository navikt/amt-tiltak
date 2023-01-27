package no.nav.amt.tiltak.kafka.producer.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerV1Dto(
	val id: UUID,
	val gjennomforingId: UUID,
	val personIdent: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatus.Type,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
)

