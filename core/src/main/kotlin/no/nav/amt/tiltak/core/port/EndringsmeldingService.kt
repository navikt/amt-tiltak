package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.LocalDate
import java.util.*

interface EndringsmeldingService {

	fun hentEndringsmelding(id: UUID): Endringsmelding

	fun markerSomFerdig(endringsmeldingId: UUID, navAnsattId: UUID)

	fun opprettMedStartDato(
		deltakerId: UUID,
		startDato: LocalDate,
		ansattId: UUID
	): Endringsmelding

	fun opprettMedSluttDato(
		deltakerId: UUID,
		sluttDato: LocalDate,
		ansattId: UUID
	): Endringsmelding

	fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding>

	fun hentEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding>

	fun hentSisteAktive(deltakerId: UUID): Endringsmelding?

	fun hentAntallAktiveForGjennomforing(gjennomforingId: UUID): Int
}
