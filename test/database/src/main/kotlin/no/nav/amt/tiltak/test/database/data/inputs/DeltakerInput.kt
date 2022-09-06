package no.nav.amt.tiltak.test.database.data.inputs

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

data class DeltakerInput(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate,
	val sluttDato: LocalDate,
	val dagerPerUke: Int,
	val prosentStilling: Float,
	val registrertDato: LocalDateTime,
	val innsokBegrunnelse: String?,
	val createdAt: ZonedDateTime = ZonedDateTime.of(2022, 2, 13, 0, 0, 0, 0, ZoneId.systemDefault())
)
