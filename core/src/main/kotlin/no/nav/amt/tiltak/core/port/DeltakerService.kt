package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.*
import java.time.LocalDate
import java.util.*

interface DeltakerService {

	fun upsertDeltaker(personIdent: String, deltaker: DeltakerUpsert)

	fun insertStatus(status: DeltakerStatusInsert)

	fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker?

	fun oppdaterStatuser()

	fun slettDeltaker(deltakerId: UUID)

	fun hentDeltakereMedPersonIdent(personIdent: String): List<Deltaker>

	fun oppdaterNavEnhet(personIdent: String, navEnhet: NavEnhet?)

	fun finnesBruker(personIdent: String): Boolean

	fun oppdaterAnsvarligVeileder(personIdent: String, navAnsattId: UUID)

	fun leggTilOppstartsdato(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun endreOppstartsdato(deltakerId: UUID, arrangorAnsattId: UUID, oppstartsdato: LocalDate)

	fun forlengDeltakelse(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate)

	fun endreDeltakelsesprosent(deltakerId: UUID, arrangorAnsattId: UUID, deltakerProsent: Int, gyldigFraDato: LocalDate?)

	fun avsluttDeltakelse(deltakerId: UUID, arrangorAnsattId: UUID, sluttdato: LocalDate, statusAarsak: DeltakerStatus.Aarsak)

	fun deltakerIkkeAktuell(deltakerId: UUID, arrangorAnsattId: UUID, statusAarsak: DeltakerStatus.Aarsak)

	fun erSkjermet(deltakerId: UUID): Boolean

	fun settSkjermet(personIdent: String, erSkjermet: Boolean)

	fun hentDeltakerMap(deltakerIder: List<UUID>): Map<UUID, Deltaker>

	fun kanDeltakerSkjulesForTiltaksarrangor(deltakerId: UUID): Boolean

	fun skjulDeltakerForTiltaksarrangor(deltakerId: UUID, arrangorAnsattId: UUID)

	fun opphevSkjulDeltakerForTiltaksarrangor(deltakerId: UUID)

	fun erSkjultForTiltaksarrangor(deltakerId: UUID): Boolean

	fun erSkjultForTiltaksarrangor(deltakerIder: List<UUID>): Map<UUID, Boolean>

	fun republiserAlleDeltakerePaKafka(batchSize: Int = 500)

}
