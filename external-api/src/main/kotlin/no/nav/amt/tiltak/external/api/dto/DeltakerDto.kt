package no.nav.amt.tiltak.external.api.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerDto (
	val id: UUID,
	val gjennomforing: GjennomforingDto,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatusDto,
	val dagerPerUke: Float?,
	val prosentStilling: Float?,
	val registrertDato: LocalDateTime,
)
