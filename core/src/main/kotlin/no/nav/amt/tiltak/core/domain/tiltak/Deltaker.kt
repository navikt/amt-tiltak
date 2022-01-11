package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Deltaker(
	val id: UUID = UUID.randomUUID(),
	val bruker: Bruker? = null,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Status,
	val statusEndret: LocalDate = LocalDate.now(),
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int?,
	val prosentStilling: Float?,
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

		return if (status != newStatus
			|| startDato != newDeltakerStartDato
			|| sluttDato != newDeltakerSluttDato
		) {

			copy(
				status = newStatus,
				startDato = newDeltakerStartDato,
				sluttDato = newDeltakerSluttDato,
				statusEndret = if (newStatus == status) statusEndret else newStatusEndretDato
			)

		} else this
	}
}

