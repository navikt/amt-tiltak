package no.nav.amt.tiltak.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerDbo(
	val internalId: Int,
	val externalId: UUID,
	val brukerInternalId: Int,
	val brukerFodselsnummer: String,
	val brukerFornavn: String,
	val brukerEtternavn: String,
	val tiltaksinstansInternalId: Int,
	val deltakerOppstartsdato: LocalDate?,
	val deltakerSluttdato: LocalDate?,
	val status: Deltaker.Status?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toDeltaker(): Deltaker {
		return Deltaker(
			fornavn = brukerFornavn,
			etternavn = brukerEtternavn,
			fodselsdato = brukerFodselsnummer,
			startdato = deltakerOppstartsdato,
			sluttdato = deltakerSluttdato,
			status = status
		)
	}

	fun update(
		newStatus: Deltaker.Status,
		newDeltakerStartDato: LocalDate?,
		newDeltakerSluttDato: LocalDate?
	): UpdateCheck<DeltakerDbo> {
		if (status != newStatus
			|| deltakerOppstartsdato != newDeltakerStartDato
			|| deltakerSluttdato != newDeltakerSluttDato
		) {

			val updatedDeltaker = this.copy(
				status = newStatus,
				deltakerOppstartsdato = newDeltakerStartDato,
				deltakerSluttdato = newDeltakerSluttDato,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updatedDeltaker)

		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}

}
