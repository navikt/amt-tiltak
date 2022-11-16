package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.time.LocalDate
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(fodselsnummer: String, deltaker: DeltakerUpsert)

	fun insertStatus(status: DeltakerStatusInsert)

	fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker?

	fun oppdaterStatuser()

	fun slettDeltaker(deltakerId: UUID)

	fun hentDeltakereMedFnr(fodselsnummer: String): List<Deltaker>

	fun oppdaterNavEnhet(fodselsnummer: String, navEnhet: NavEnhet?)

	fun finnesBruker(fodselsnummer: String): Boolean

	fun oppdaterAnsvarligVeileder(fodselsnummer: String, navAnsattId: UUID)

	fun leggTilOppstartsdato(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun endreOppstartsdato(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun forlengDeltakelse(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate)

	fun avsluttDeltakelse(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate, statusAarsak: Deltaker.StatusAarsak)

	fun deltakerIkkeAktuell(deltakerId: UUID, arrangorAnsattId: UUID, statusAarsak: Deltaker.StatusAarsak)

	fun erSkjermet(deltakerId: UUID): Boolean
}
