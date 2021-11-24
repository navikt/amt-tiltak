package no.nav.amt.tiltak.tiltaksleverandor.dbo

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import no.nav.amt.tiltak.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import java.time.LocalDateTime
import java.util.*

data class TiltaksleverandorDbo(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toTiltaksleverandor(): Tiltaksleverandor {
		return Tiltaksleverandor(
			id = id,
			organisasjonsnummer = organisasjonsnummer,
			navn = navn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
		)
	}

	fun update(other: TiltaksleverandorDbo): UpdateCheck<TiltaksleverandorDbo> {
		if (this != other) {
			val updated = this.copy(
				overordnetEnhetOrganisasjonsnummer = other.overordnetEnhetOrganisasjonsnummer,
				overordnetEnhetNavn = other.overordnetEnhetNavn,
				organisasjonsnummer = other.organisasjonsnummer,
				navn = other.navn,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updated)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}

}
