package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.util.*

data class DeltakerStatuser(
	val statuser: List<DeltakerStatus>
) {

	companion object {
		fun aktivStatus(status: Deltaker.Status, endretDato: LocalDate = LocalDate.now()) =
			DeltakerStatuser(listOf(DeltakerStatus(
				status = status,
				endretDato = endretDato,
				aktiv = true
			)))
	}

	val current: DeltakerStatus = requireNotNull(statuser.find { it.aktiv }) { "Deltaker må ha minst en aktiv status" }

	fun medNy(status: Deltaker.Status, endretDato: LocalDate) = DeltakerStatuser(
		statuser.map { it.deaktiver() } + DeltakerStatus(status = status, endretDato = endretDato, aktiv = true)
	)
}

data class DeltakerStatus(
	val id: UUID = UUID.randomUUID(),
	val status: Deltaker.Status,
	val endretDato: LocalDate,
	val aktiv: Boolean = false
) {
	fun deaktiver(): DeltakerStatus = copy(aktiv = false)
}
