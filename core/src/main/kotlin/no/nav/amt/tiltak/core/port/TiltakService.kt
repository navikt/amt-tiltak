package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak

interface TiltakService {

	fun addTiltak(tiltak: Tiltak): Tiltak

}
