package no.nav.amt.tiltak.veileder

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import java.time.ZonedDateTime
import java.util.*

data class ArrangorVeilederDbo(
	val id: UUID,
	val ansattId: UUID,
	val deltakerId: UUID,
	val erMedveileder: Boolean,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime,
) {
	fun toArrangorVeileder(): ArrangorVeileder {
		return ArrangorVeileder(id, ansattId, deltakerId, erMedveileder)
	}
}


