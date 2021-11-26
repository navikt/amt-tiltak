package no.nav.amt.tiltak.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Fodselsnummer
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltakerDto
import no.nav.amt.tiltak.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerDbo(
	val id: UUID,
	val brukerId: UUID,
	val brukerFodselsnummer: Fodselsnummer,
	val brukerFornavn: String,
	val brukerEtternavn: String,
	val tiltakInstansId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val arenaStatus: String?,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
	val status: Deltaker.Status?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toDeltaker(): Deltaker {
		return Deltaker(
			id = id,
			fornavn = brukerFornavn,
			etternavn = brukerEtternavn,
			fodselsdato = brukerFodselsnummer,
			startdato = startDato,
			sluttdato = sluttDato,
			status = status
		)
	}

	fun toDto(): TiltakDeltakerDto {
		return TiltakDeltakerDto(
			id = id,
			fornavn = brukerFornavn,
			etternavn = brukerEtternavn,
			fodselsdato = brukerFodselsnummer.toFodselDato(),
			startdato = startDato,
			sluttdato = sluttDato,
			status = status
		)
	}

	fun update(
		newStatus: Deltaker.Status,
		newDeltakerStartDato: LocalDate?,
		newDeltakerSluttDato: LocalDate?
	): UpdateCheck<DeltakerDbo> {
		if (status != newStatus
			|| startDato != newDeltakerStartDato
			|| sluttDato != newDeltakerSluttDato
		) {

			val updatedDeltaker = this.copy(
				status = newStatus,
				startDato = newDeltakerStartDato,
				sluttDato = newDeltakerSluttDato,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updatedDeltaker)

		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}

}
