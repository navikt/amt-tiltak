package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.utils.UpdateStatus
import java.time.LocalDateTime
import java.util.*

data class ArrangorDbo(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toArrangor(): Arrangor {
		return Arrangor(
			id = id,
			organisasjonsnummer = organisasjonsnummer,
			navn = navn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			overordnetEnhetNavn = overordnetEnhetNavn,
		)
	}

	fun update(other: ArrangorDbo): UpdateCheck<ArrangorDbo> {
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
