package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import java.time.LocalDateTime
import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype

data class RegistrerVurderingRequest(
	val id: UUID,
	val opprettet: LocalDateTime,
    val vurderingstype: Vurderingstype,
    val begrunnelse: String?
)
