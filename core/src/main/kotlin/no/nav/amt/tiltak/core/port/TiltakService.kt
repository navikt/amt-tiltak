package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface TiltakService {
	fun upsertUpdateTiltak(arenaId: String, navn: String, kode: String): Tiltak

	fun getTiltakFromArenaId(arenaId: String): Tiltak?

	fun upsertTiltaksinstans(
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

	fun upsertDeltaker(
		tiltaksgjennomforing: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status = Deltaker.Status.NY_BRUKER
	): Deltaker
}
