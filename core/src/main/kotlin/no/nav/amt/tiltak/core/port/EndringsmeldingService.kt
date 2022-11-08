package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.LocalDate
import java.util.*

interface EndringsmeldingService {

	fun hentEndringsmelding(id: UUID): Endringsmelding

	fun markerSomUtfort(endringsmeldingId: UUID, navAnsattId: UUID)

	fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding>

	fun hentEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding>

	fun hentAktive(deltakerIder: List<UUID>): Map<UUID, List<Endringsmelding>>

	fun hentAntallAktiveForGjennomforing(gjennomforingId: UUID): Int

	fun opprettLeggTilOppstartsdatoEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun opprettEndreOppstartsdatoEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun opprettForlengDeltakelseEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate)

	fun opprettAvsluttDeltakelseEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate, statusAarsak: Deltaker.StatusAarsak)
	fun opprettDeltakerIkkeAktuellEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, statusAarsak: Deltaker.StatusAarsak)
}
