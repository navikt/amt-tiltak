package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.UUID

data class Vurdering(
	val id: UUID,
	val deltakerId: UUID,
	val vurderingstype: Vurderingstype,
	val begrunnelse: String?,
	val opprettetAvArrangorAnsattId: UUID,
	val opprettet: LocalDateTime,
)
