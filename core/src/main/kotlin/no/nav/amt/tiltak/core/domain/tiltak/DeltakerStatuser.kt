package no.nav.amt.tiltak.core.domain.tiltak
import java.time.LocalDateTime
import java.util.*

data class DeltakerStatuser(
	val statuser: List<DeltakerStatus>
) {
	init {
	    require(statuser.filter { it.aktiv }.size == 1) { "Kan kun ha en aktiv status"}
	}

	companion object {
		fun settAktivStatus(status: Deltaker.Status, endretDato: LocalDateTime = LocalDateTime.now()) =
			DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = status,
				endretDato = endretDato
			)))
	}

	val current: DeltakerStatus = statuser.find { it.aktiv }!!

	fun medNy(status: Deltaker.Status, endretDato: LocalDateTime = LocalDateTime.now()) = DeltakerStatuser(
		statuser.map { it.deaktiver() } + DeltakerStatus.nyAktiv(status = status, endretDato = endretDato)
	)
}

data class DeltakerStatus(
	val id: UUID = UUID.randomUUID(),
	val status: Deltaker.Status,
	val endretDato: LocalDateTime,
	val aktiv: Boolean = false
) {

	companion object {
		fun nyAktiv(status: Deltaker.Status, endretDato: LocalDateTime = LocalDateTime.now()) =
			DeltakerStatus(status = status, endretDato = endretDato, aktiv = true)

		fun nyInaktiv(status: Deltaker.Status, endretDato: LocalDateTime = LocalDateTime.now()) =
			DeltakerStatus(status = status, endretDato = endretDato, aktiv = false)
	}

	fun deaktiver(): DeltakerStatus = copy(aktiv = false)
}
