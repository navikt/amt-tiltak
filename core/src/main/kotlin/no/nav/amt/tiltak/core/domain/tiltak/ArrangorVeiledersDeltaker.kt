package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ArrangorVeiledersDeltaker(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val fodselsnummer: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatus.Type,
	val statusDato: LocalDateTime,
	val gjennomforingId: UUID,
	val gjennomforingNavn: String,
	val gjennomforingType: String,
	val arrangorId: UUID,
	val erMedveilederFor: Boolean
)
