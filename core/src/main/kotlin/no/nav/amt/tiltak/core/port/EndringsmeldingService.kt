package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak
import java.time.LocalDate
import java.util.*

interface EndringsmeldingService {

	fun hentEndringsmelding(id: UUID): Endringsmelding

	fun markerSomUtfort(endringsmeldingId: UUID, navAnsattId: UUID)

	fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding>

	fun hentAktiveEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding>

	fun hentAktiveEndringsmeldingerForDeltakere(deltakerIder: List<UUID>): Map<UUID, List<Endringsmelding>>

	fun opprettLeggTilOppstartsdatoEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun opprettEndreOppstartsdatoEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun opprettForlengDeltakelseEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate)

	fun opprettEndreDeltakelseProsentEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, deltakerProsent: Int, dagerPerUke: Int?, gyldigFraDato: LocalDate?)

	fun opprettAvsluttDeltakelseEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate, statusAarsak: EndringsmeldingStatusAarsak)

	fun opprettDeltakerIkkeAktuellEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, statusAarsak: EndringsmeldingStatusAarsak)

    fun markerSomTilbakekalt(id: UUID)

	fun hentAktiveEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding>

	fun slett(deltakerId: UUID)
	fun opprettErAktuellEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID)
	fun opprettEndresluttdatoEndringsmelding(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate)
}
