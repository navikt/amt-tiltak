package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.ZonedDateTime
import java.util.*

data class EndringsmeldingInput(
	val id: UUID,
	val deltakerId: UUID,
	val utfortAvNavAnsattId: UUID? = null,
	val utfortTidspunkt: ZonedDateTime? = null,
	val opprettetAvArrangorAnsattId: UUID,
	val status: Endringsmelding.Status,
	val type: String,
	val innhold: String,
	val createdAt: ZonedDateTime = ZonedDateTime.now(),
	val modifiedAt: ZonedDateTime = ZonedDateTime.now(),
)
