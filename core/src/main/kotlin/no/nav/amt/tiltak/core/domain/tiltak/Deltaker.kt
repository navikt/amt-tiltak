package no.nav.amt.tiltak.core.domain.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.VENTER_PA_OPPSTART
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Deltaker(
	val id: UUID = UUID.randomUUID(),
	val gjennomforingId: UUID,
	val bruker: Bruker? = null,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val statuser: DeltakerStatuser,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
) {

	val status = statuser.current.status

	enum class Status {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL, FEILREGISTRERT
	}

	fun oppdater(nyDeltaker: Deltaker) : Deltaker {
		if (nyDeltaker.id != id) throw IllegalStateException("Kan ikke oppdatere deltaker id ${id} med en annen deltaker id")
		return nyDeltaker.copy(
			statuser = oppdaterStatus(nyDeltaker.status, nyDeltaker.statuser.current.endretDato),
			bruker = bruker
		)
	}

	fun oppdaterStatus(newStatus: Status, newStatusEndretDato: LocalDateTime = LocalDateTime.now()): DeltakerStatuser {
		return if (statuser.current.status != newStatus) statuser.medNy(newStatus, newStatusEndretDato) else statuser
	}

	fun progressStatus(): Deltaker {
		val now = LocalDate.now()

		val sluttDato = sluttDato ?: LocalDate.now().plusYears(1000)

		if(status == VENTER_PA_OPPSTART && startDato == null) return this

		if(status == VENTER_PA_OPPSTART && now.isAfter(startDato!!.minusDays(1)) && now.isBefore(sluttDato.plusDays(1)))
			return copy(statuser = statuser.medNy(Status.DELTAR))

		if(listOf(Status.DELTAR, VENTER_PA_OPPSTART).contains(status) && now.isAfter(sluttDato))
			return copy(statuser = statuser.medNy(Status.HAR_SLUTTET))

		return this
	}
}

