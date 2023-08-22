package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype

data class RegistrerVurderingRequest(
    val vurderingstype: Vurderingstype,
    val begrunnelse: String?
)
