package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Deltaker(
	val id: UUID = UUID.randomUUID(),
	val bruker: Bruker? = null,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val statuser: DeltakerStatuser,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
) {

	enum class Status {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL
	}

	fun updateStatus(
		newStatus: Status,
		newDeltakerStartDato: LocalDate?,
		newDeltakerSluttDato: LocalDate?,
		newStatusEndretDato: LocalDate = LocalDate.now()
	): Deltaker {

		return if (statuser.current.status != newStatus
			|| startDato != newDeltakerStartDato
			|| sluttDato != newDeltakerSluttDato
		) {

			copy(
				startDato = newDeltakerStartDato,
				sluttDato = newDeltakerSluttDato,
				statuser = if(statuser.current.status == newStatus) statuser else statuser.medNy(newStatus, newStatusEndretDato),
			)

		} else this
	}

	val status = statuser.current.status
}

