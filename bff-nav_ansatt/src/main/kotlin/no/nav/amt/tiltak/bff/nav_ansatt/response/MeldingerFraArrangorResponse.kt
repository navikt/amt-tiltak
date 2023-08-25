package no.nav.amt.tiltak.bff.nav_ansatt.response

import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.VurderingDto

data class MeldingerFraArrangorResponse(
	val endringsmeldinger: List<EndringsmeldingDto>,
	val vurderinger: List<VurderingDto>
)
