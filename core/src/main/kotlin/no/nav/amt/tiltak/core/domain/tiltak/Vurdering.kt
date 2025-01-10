package no.nav.amt.tiltak.core.domain.tiltak

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
data class Vurdering(
	val id: UUID,
	val deltakerId: UUID,
	val vurderingstype: Vurderingstype,
	val begrunnelse: String?,
	val opprettetAvArrangorAnsattId: UUID,
	val opprettet: LocalDateTime,
)
