package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import java.time.LocalDateTime
import java.util.UUID

data class VurderingDto(
	val id: UUID,
	val deltaker: DeltakerDto,
	val vurderingstype: Vurderingstype,
	val begrunnelse: String?,
	val opprettetDato: LocalDateTime
)
