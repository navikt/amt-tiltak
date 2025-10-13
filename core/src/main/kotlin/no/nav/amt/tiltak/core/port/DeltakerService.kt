package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import java.time.LocalDateTime
import java.util.UUID

interface DeltakerService {

	fun upsertDeltaker(personIdent: String, deltaker: DeltakerUpsert, erKometMaster: Boolean): Deltaker

	fun insertStatus(status: DeltakerStatusInsert, erKometDeltaker: Boolean)

	fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker>

	fun hentDeltaker(deltakerId: UUID): Deltaker?

	fun hentDeltakere(deltakerIder: List<UUID>): List<Deltaker>

	fun progressStatuser()

	fun slettDeltaker(deltakerId: UUID, erKometDeltaker: Boolean?, erEnkeltplassDeltaker: Boolean)

	fun hentDeltakereMedPersonIdent(personIdent: String): List<Deltaker>

	fun hentDeltakereMedPersonId(brukerId: UUID): List<Deltaker>

	fun erSkjermet(deltakerId: UUID): Boolean

	fun hentDeltakerMap(deltakerIder: List<UUID>): Map<UUID, Deltaker>

	fun republiserAlleDeltakerePaKafka(batchSize: Int = 500, publiserInternTopic: Boolean = true, publiserEksternTopic: Boolean = true)

	fun republiserDeltakerePaDeltakerV2(tiltakstype: String)

	fun publiserDeltakerPaDeltakerV2Kafka(deltakerId: UUID)

	fun slettDeltakerePaaGjennomforing(gjennomforingId: UUID)

	fun avsluttDeltakerePaaAvbruttGjennomforing(gjennomforingId: UUID)

	fun lagreVurdering(vurdering: Vurdering): List<Vurdering>


	fun delMedArrangor(deltakerIder: List<UUID>)
	fun publiserDeltakerPaKafka(deltakerId: UUID, endretDato: LocalDateTime)
}
