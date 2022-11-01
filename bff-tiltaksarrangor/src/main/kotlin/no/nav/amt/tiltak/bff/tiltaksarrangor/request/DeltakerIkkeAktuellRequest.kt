package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import no.nav.amt.tiltak.bff.tiltaksarrangor.type.DeltakerStatusAarsak

data class DeltakerIkkeAktuellRequest (
    val aarsak: DeltakerStatusAarsak,
)
