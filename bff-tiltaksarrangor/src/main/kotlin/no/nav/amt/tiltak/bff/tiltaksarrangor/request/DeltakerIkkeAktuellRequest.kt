package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.EndringsmeldingStatusAarsakDto

data class DeltakerIkkeAktuellRequest (
	val aarsak: EndringsmeldingStatusAarsakDto,
)
