package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import java.time.LocalDateTime
import java.util.*

data class TiltakDbo(
	val internalId: Int,
	val externalId: UUID,
	val arenaId: String,
	val navn: String,
	val type: String,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toTiltak(): Tiltak {
		return Tiltak(
			id = externalId,
			navn = navn,
			kode = type
		)
	}

	fun update(other: TiltakDbo): UpdateCheck<TiltakDbo> {
		if (this != other) {
			val updatedTiltak = this.copy(
				navn = other.navn,
				type = other.type,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updatedTiltak)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}
}
