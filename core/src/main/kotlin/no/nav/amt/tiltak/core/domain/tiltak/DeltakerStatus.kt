package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.*

data class DeltakerStatus(
	val id: UUID = UUID.randomUUID(),
	val status: Deltaker.Status,
	val statusGjelderFra: LocalDateTime,
	val opprettetDato: LocalDateTime?,
	val aktiv: Boolean = false
) {

	companion object {
		fun nyAktiv(status: Deltaker.Status, gjelderFra: LocalDateTime = LocalDateTime.now()) =
			DeltakerStatus(status = status, statusGjelderFra = gjelderFra, aktiv = true, opprettetDato = null)

		fun nyInaktiv(status: Deltaker.Status, gjelderFra: LocalDateTime = LocalDateTime.now()) =
			DeltakerStatus(status = status, statusGjelderFra = gjelderFra, aktiv = false, opprettetDato = null)
	}

	fun deaktiver(): DeltakerStatus = copy(aktiv = false)
}

