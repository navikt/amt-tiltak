package no.nav.amt.tiltak.tiltaksleverandor.dbo

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import no.nav.amt.tiltak.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import java.time.LocalDateTime
import java.util.*

data class TiltaksleverandorDbo(
	val internalId: Int,
	val externalId: UUID,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toTiltaksleverandor(): Tiltaksleverandor {
		return Tiltaksleverandor(
			id = externalId,
			organisasjonsnummer = organisasjonsnummer,
			organisasjonsnavn = organisasjonsnavn,
			virksomhetsnummer = virksomhetsnummer,
			virksomhetsnavn = virksomhetsnavn
		)
	}

	fun update(other: TiltaksleverandorDbo): UpdateCheck<TiltaksleverandorDbo> {
		if (this != other) {
			val updated = this.copy(
				organisasjonsnummer = other.organisasjonsnummer,
				organisasjonsnavn = other.organisasjonsnavn,
				virksomhetsnummer = other.virksomhetsnummer,
				virksomhetsnavn = other.virksomhetsnavn,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updated)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}

}
