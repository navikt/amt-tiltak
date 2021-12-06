package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface TiltakInstansService {
	fun getTiltakInstanserForArrangor(arrangorId: UUID): List<TiltakInstans>

	fun getTiltakInstans(id: UUID): TiltakInstans

	fun upsertTiltaksinstans(
		arenaId: Int,
		tiltakId: UUID,
		tiltaksarrangorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltakInstans

	fun getTiltaksinstansFromArenaId(arenaId: Int): TiltakInstans?

}
