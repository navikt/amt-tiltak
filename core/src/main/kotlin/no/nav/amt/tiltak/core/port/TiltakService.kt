package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface TiltakService {
	fun addUpdateTiltak(arenaId: String, navn: String, kode: String): Tiltak

	fun getTiltakFromArenaId(arenaId: String): Tiltak?

	fun addUpdateTiltaksinstans(
		arenaId: Int,
		tiltakId: UUID,
		tiltaksleverandorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltakInstans

	fun getTiltaksinstansFromArenaId(arenaId: Int): TiltakInstans?
}
