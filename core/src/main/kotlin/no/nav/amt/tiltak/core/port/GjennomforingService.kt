package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface GjennomforingService {

	fun getGjennomforingerForArrangor(arrangorId: UUID): List<Gjennomforing>

	fun getGjennomforing(id: UUID): Gjennomforing

	fun upsertGjennomforing(
		id: UUID,
		tiltakId: UUID,
		arrangorId: UUID,
		navn: String,
		status: Gjennomforing.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): Gjennomforing

}
