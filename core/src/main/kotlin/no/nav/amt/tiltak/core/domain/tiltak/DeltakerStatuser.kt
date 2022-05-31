package no.nav.amt.tiltak.core.domain.tiltak
import java.time.LocalDateTime

data class DeltakerStatuser(
	val statuser: List<DeltakerStatus>
) {
	init {
	    require(statuser.filter { it.aktiv }.size == 1) {
			"Kan kun ha en aktiv status, deltakerstatuser med id ${statuser.map { it.id }.joinToString(",")}"
		}
	}

	companion object {
		fun medNyAktiv(status: Deltaker.Status, endretDato: LocalDateTime = LocalDateTime.now()) =
			DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(
				status = status,
				gjelderFra = endretDato
			)))
	}

	val current: DeltakerStatus = statuser.find { it.aktiv }!!

	fun medNy(status: Deltaker.Status, gjelderFra: LocalDateTime = LocalDateTime.now()) = DeltakerStatuser(
		statuser.map { it.deaktiver() } + DeltakerStatus.nyAktiv(status = status, gjelderFra = gjelderFra)
	)
}
