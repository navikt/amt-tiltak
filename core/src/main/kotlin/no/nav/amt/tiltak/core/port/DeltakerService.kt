package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.*
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

	fun endreDeltakelsesprosent(deltakerId: UUID, id: UUID, deltakerProsent: Int)

	fun avsluttDeltakelse(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate, statusAarsak: DeltakerStatus.Aarsak)

	fun deltakerIkkeAktuell(deltakerId: UUID, arrangorAnsattId: UUID, statusAarsak: DeltakerStatus.Aarsak)

	fun erSkjermet(deltakerId: UUID): Boolean

	fun hentDeltakerMap(deltakerIder: List<UUID>): Map<UUID, Deltaker>

}
