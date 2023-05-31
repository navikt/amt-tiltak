package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.util.UUID

interface DeltakerService {

	fun upsertDeltaker(personIdent: String, deltaker: DeltakerUpsert)

	fun insertStatus(status: DeltakerStatusInsert)

	fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker?

	fun hentDeltakere(deltakerIder: List<UUID>): List<Deltaker>

	fun progressStatuser()

	fun slettDeltaker(deltakerId: UUID)

	fun hentDeltakereMedPersonIdent(personIdent: String): List<Deltaker>

	fun oppdaterNavEnhet(personIdent: String, navEnhet: NavEnhet?)

	fun finnesBruker(personIdent: String): Boolean

	fun oppdaterAnsvarligVeileder(personIdent: String, navAnsattId: UUID)

	fun erSkjermet(deltakerId: UUID): Boolean

	fun settSkjermet(personIdent: String, erSkjermet: Boolean)

	fun hentDeltakerMap(deltakerIder: List<UUID>): Map<UUID, Deltaker>

	fun kanDeltakerSkjulesForTiltaksarrangor(deltakerId: UUID): Boolean

	fun skjulDeltakerForTiltaksarrangor(deltakerId: UUID, arrangorAnsattId: UUID)

	fun opphevSkjulDeltakerForTiltaksarrangor(deltakerId: UUID)

	fun erSkjultForTiltaksarrangor(deltakerId: UUID): Boolean

	fun erSkjultForTiltaksarrangor(deltakerIder: List<UUID>): Map<UUID, Boolean>

	fun republiserAlleDeltakerePaKafka(batchSize: Int = 500)

	fun slettDeltakerePaaGjennomforing(gjennomforingId: UUID)

	fun hentBrukerInfo(deltakerId: UUID): Map<String, UUID?>

}
