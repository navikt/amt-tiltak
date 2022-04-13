package no.nav.amt.tiltak.core.domain.tiltak

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Endringsmelding (
	val id: UUID,
	val bruker: Bruker,
	val startDato: LocalDate?,
	val aktiv: Boolean,
	val godkjent: Boolean,
	val arkivert: Boolean,
	val opprettetAvArrangorAnsatt: Ansatt,
	val opprettetDato: LocalDateTime

)
