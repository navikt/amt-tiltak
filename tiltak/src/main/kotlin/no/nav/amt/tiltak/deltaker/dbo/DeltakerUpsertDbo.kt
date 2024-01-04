package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Mal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerUpsertDbo(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Float? = null,
	val prosentStilling: Float? = null,
	val innsokBegrunnelse: String? = null,
	val mal: List<Mal>?
)
