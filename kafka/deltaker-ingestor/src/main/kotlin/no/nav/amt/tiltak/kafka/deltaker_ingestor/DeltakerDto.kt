package no.nav.amt.tiltak.kafka.deltaker_ingestor

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Mal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerDto(
	val id: UUID,
	val personident: String,
	val deltakerlisteId: UUID,
	val startdato: LocalDate?,
	val sluttdato: LocalDate?,
	val dagerPerUke: Float?,
	val deltakelsesprosent: Float?,
	val bakgrunnsinformasjon: String?,
	val mal: List<Mal>,
	val status: DeltakerStatusDto,
	val sistEndret: LocalDateTime,
	val opprettet: LocalDateTime,
)

data class DeltakerStatusDto(
	val id: UUID,
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak?,
	val gyldigFra: LocalDateTime,
	val gyldigTil: LocalDateTime?,
	val opprettet: LocalDateTime,
)
