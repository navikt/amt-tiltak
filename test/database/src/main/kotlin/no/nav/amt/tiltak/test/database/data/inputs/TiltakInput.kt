package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.util.*

data class TiltakInput(
	val id: UUID,
	val navn: String,
	val type: String
) {
	fun toTiltak(): Tiltak {
		return Tiltak(
			id = this.id,
			kode = this.type,
			navn = this.navn
		)
	}
}

