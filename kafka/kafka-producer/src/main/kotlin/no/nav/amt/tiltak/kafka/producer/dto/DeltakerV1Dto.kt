package no.nav.amt.tiltak.kafka.producer.dto

import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerV1Dto(
	val id: UUID,
	val gjennomforingId: UUID,
	val personIdent: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatusDto,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Float?,
	val prosentStilling: Float?,
	val endretDato: LocalDateTime,
	val kilde: Kilde?
)

