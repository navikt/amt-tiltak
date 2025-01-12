package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.UUID

data class VurderingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val vurderingstype: Vurderingstype,
	val begrunnelse: String?,
	val opprettetAvArrangorAnsattId: UUID,
	val gyldigFra: LocalDateTime,
	val gyldigTil: LocalDateTime?
) {
	fun toVurdering(): Vurdering =
		Vurdering(
			id = id,
			deltakerId = deltakerId,
			vurderingstype = vurderingstype,
			begrunnelse = begrunnelse,
			opprettetAvArrangorAnsattId = opprettetAvArrangorAnsattId,
			opprettet = gyldigFra
		)
}
