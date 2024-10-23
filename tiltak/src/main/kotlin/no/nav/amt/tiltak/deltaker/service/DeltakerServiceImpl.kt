package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.core.domain.tiltak.harIkkeStartet
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.kafka.DeltakerV1ProducerService
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerUpsertDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
	private val endringsmeldingService: EndringsmeldingService,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate,
	private val deltakerV1Producer: DeltakerV1ProducerService,
	private val publisherService: DataPublisherService,
	private val vurderingRepository: VurderingRepository,
	private val unleashService: UnleashService
) : DeltakerService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsertDeltaker(personIdent: String, deltaker: DeltakerUpsert, erKometMaster: Boolean) {
		val lagretDeltaker = hentDeltaker(deltaker.id)
		val brukerId = brukerService.getIdOrCreate(personIdent)

		if (lagretDeltaker == null || !deltaker.compareTo(lagretDeltaker)) {
			val deltakerUpsertDbo = deltaker.toUpsertDbo(brukerId)
			deltakerRepository.upsert(deltakerUpsertDbo)
			oppdaterStatus(deltaker.statusInsert)

			val oppdatertDeltaker = hentDeltaker(deltaker.id)
				?: throw IllegalStateException("Fant ikke deltaker med id ${deltaker.id}")

			publiser(oppdatertDeltaker, LocalDateTime.now(), erKometMaster)
		}
	}

	override fun insertStatus(status: DeltakerStatusInsert, erKometDeltaker: Boolean) {
		transactionTemplate.executeWithoutResult {
			val statusBleOppdatert = oppdaterStatus(status)

			if (statusBleOppdatert) {
				val oppdatertDeltaker = hentDeltaker(status.deltakerId)
					?: throw IllegalStateException("Fant ikke deltaker med id ${status.deltakerId}")

				publiser(oppdatertDeltaker, LocalDateTime.now(), erKometDeltaker)
			}
		}
	}

	override fun hentDeltakerePaaGjennomforing(gjennomforingId: UUID): List<Deltaker> {
		val deltakere = deltakerRepository.getDeltakerePaaTiltak(gjennomforingId)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun hentDeltaker(deltakerId: UUID): Deltaker? {
		val deltaker = deltakerRepository.get(deltakerId)
			?: return null

		return deltaker.toDeltaker(hentStatusOrThrow(deltakerId))
	}

	override fun hentDeltakere(deltakerIder: List<UUID>): List<Deltaker> {
		val deltakere = deltakerRepository.getDeltakere(deltakerIder)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun hentDeltakereMedPersonIdent(personIdent: String): List<Deltaker> {
		val deltakere = deltakerRepository.getDeltakereMedPersonIdent(personIdent)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun hentDeltakereMedPersonId(brukerId: UUID): List<Deltaker> {
		val deltakere = deltakerRepository.getDeltakereMedBrukerId(brukerId)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun progressStatuser() {
		val deltakereSomSkalAvsluttes = deltakerRepository.erPaaAvsluttetGjennomforing()
			.plus(deltakerRepository.sluttDatoPassert())

		val gjennomforingIder = deltakereSomSkalAvsluttes
			.map { it.gjennomforingId }.distinct()
		val erKometMasterForGjennomforingMap = gjennomforingIder
			.associateWith { unleashService.erKometMasterForTiltakstype(gjennomforingService.getGjennomforing(it).tiltak.kode) }
			.toMutableMap()

		val deltakere = deltakereSomSkalAvsluttes
			.filterNot { erKometMasterForGjennomforingMap[it.gjennomforingId]!! }
			.let { mapDeltakereOgAktiveStatuser(it) }

		progressStatuser(deltakere)

		val deltakereSomSkalDelta = deltakerRepository.skalHaStatusDeltar()

		deltakereSomSkalDelta.forEach {
			if (erKometMasterForGjennomforingMap[it.gjennomforingId] == null) {
				erKometMasterForGjennomforingMap[it.gjennomforingId] = unleashService.erKometMasterForTiltakstype(gjennomforingService.getGjennomforing(it.gjennomforingId).tiltak.kode)
			}
		}

		oppdaterStatuser(
			deltakereSomSkalDelta.filterNot { erKometMasterForGjennomforingMap[it.gjennomforingId]!! }.map { it.id },
			nyStatus = DeltakerStatus.Type.DELTAR
		)
	}

	override fun slettDeltakerePaaGjennomforing(gjennomforingId: UUID) {
		val tiltaktype = gjennomforingService.getGjennomforing(gjennomforingId).tiltak.kode
		val erKometDeltaker = unleashService.erKometMasterForTiltakstype(tiltaktype)
		if (erKometDeltaker) {
			return
		} else {
			hentDeltakerePaaGjennomforing(gjennomforingId).forEach {
				slettDeltaker(it.id, erKometDeltaker)
			}
		}
	}

	override fun avsluttDeltakerePaaAvbruttGjennomforing(gjennomforingId: UUID) {
		val tiltaktype = gjennomforingService.getGjennomforing(gjennomforingId).tiltak.kode
		if (unleashService.erKometMasterForTiltakstype(tiltaktype)) {
			return
		} else {
			val deltakere = hentDeltakerePaaGjennomforing(gjennomforingId)
			progressStatuser(deltakere, DeltakerStatus.Aarsak.SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT)
		}
	}

	override fun slettDeltaker(deltakerId: UUID, erKometDeltaker: Boolean?) {
		transactionTemplate.execute {
			endringsmeldingService.slett(deltakerId)
			deltakerStatusRepository.slett(deltakerId)
			vurderingRepository.slett(deltakerId)
			deltakerRepository.slettVeilederrelasjonOgDeltaker(deltakerId)
			if (erKometDeltaker != true) {
				deltakerV1Producer.publiserSlettDeltaker(deltakerId)
			}
		}

		log.info("Deltaker med id=$deltakerId er slettet")
		publisherService.publish(deltakerId, DataPublishType.DELTAKER, erKometDeltaker)
	}

	override fun erSkjermet(deltakerId: UUID): Boolean {
		val deltaker = hentDeltaker(deltakerId) ?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
		return deltaker.erSkjermet
	}

	override fun lagreVurdering(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		vurderingstype: Vurderingstype,
		begrunnelse: String?
	): List<Vurdering> {
		val status = deltakerStatusRepository.getStatusForDeltaker(deltakerId)
		if (status?.type != DeltakerStatus.Type.VURDERES) {
			log.error("Kan ikke opprette vurdering for deltaker med id $deltakerId som ikke har status VURDERES")
			throw ValidationException("Kan ikke opprette vurdering for deltaker som ikke har status VURDERES")
		}

		val opprinneligeVurderinger = vurderingRepository.getVurderingerForDeltaker(deltakerId)
		val forrigeVurdering = opprinneligeVurderinger.firstOrNull { it.gyldigTil == null }
		if (forrigeVurdering?.vurderingstype == vurderingstype && forrigeVurdering.begrunnelse == begrunnelse) return opprinneligeVurderinger

		val nyVurdering = Vurdering(
			id = UUID.randomUUID(),
			deltakerId = deltakerId,
			vurderingstype = vurderingstype,
			begrunnelse = begrunnelse,
			opprettetAvArrangorAnsattId = arrangorAnsattId,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		)

		transactionTemplate.executeWithoutResult {
			forrigeVurdering?.let { vurderingRepository.deaktiver(it.id) }
			vurderingRepository.insert(nyVurdering)
		}

		publisherService.publish(deltakerId, DataPublishType.DELTAKER, null)
		return vurderingRepository.getVurderingerForDeltaker(deltakerId)
	}

	override fun konverterStatuserForDeltakerePaaGjennomforing(
		gjennomforingId: UUID,
		oppdatertGjennomforingErKurs: Boolean
	) {
		val tiltaktype = gjennomforingService.getGjennomforing(gjennomforingId).tiltak.kode
		if (unleashService.erKometMasterForTiltakstype(tiltaktype)) {
			return
		} else {
			val deltakere = hentDeltakerePaaGjennomforing(gjennomforingId)
			if (deltakere.isNotEmpty()) {
				if (oppdatertGjennomforingErKurs) {
					konverterDeltakerstatuseFraLopendeInntakTilKurs(deltakere, gjennomforingId)
				} else {
					konverterDeltakerstatuseFraKursTilLopendeInntak(deltakere)
				}
			}
		}
	}

	private fun konverterDeltakerstatuseFraKursTilLopendeInntak(deltakere: List<Deltaker>) {
		val deltakereSomSkalOppdateres =
			deltakere.filter { it.status.type == DeltakerStatus.Type.AVBRUTT || it.status.type == DeltakerStatus.Type.FULLFORT }

		deltakereSomSkalOppdateres.forEach {
			insertStatus(
				DeltakerStatusInsert(
					id = UUID.randomUUID(),
					deltakerId = it.id,
					type = DeltakerStatus.Type.HAR_SLUTTET,
					aarsak = it.status.aarsak,
					aarsaksbeskrivelse = it.status.aarsaksbeskrivelse,
					gyldigFra = LocalDateTime.now()
				),
				erKometDeltaker = false
			)
		}
		log.info("Oppdatert status for ${deltakereSomSkalOppdateres.size} deltakere på gjennomføring som gikk fra kurs til løpende inntak")
	}

	private fun konverterDeltakerstatuseFraLopendeInntakTilKurs(deltakere: List<Deltaker>, gjennomforingId: UUID) {
		val deltakereSomSkalOppdateres =
			deltakere.filter { it.status.type == DeltakerStatus.Type.HAR_SLUTTET }

		if (deltakereSomSkalOppdateres.isNotEmpty()) {
			val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

			deltakereSomSkalOppdateres.forEach {
				insertStatus(
					DeltakerStatusInsert(
						id = UUID.randomUUID(),
						deltakerId = it.id,
						type = getDeltakerStatusType(
							deltakerSluttdato = it.sluttDato,
							gjennomforingSluttdato = gjennomforing.sluttDato
						),
						aarsak = it.status.aarsak,
						aarsaksbeskrivelse = it.status.aarsaksbeskrivelse,
						gyldigFra = LocalDateTime.now()
					),
					erKometDeltaker = false
				)
			}
		}
		log.info("Oppdatert status for ${deltakereSomSkalOppdateres.size} deltakere på gjennomføring som gikk fra løpende inntak til kurs")
	}

	private fun getDeltakerStatusType(deltakerSluttdato: LocalDate?, gjennomforingSluttdato: LocalDate?): DeltakerStatus.Type {
		return if (gjennomforingSluttdato == null || deltakerSluttdato == null) {
			DeltakerStatus.Type.FULLFORT
		} else if (deltakerSluttdato.isBefore(gjennomforingSluttdato)) {
			DeltakerStatus.Type.AVBRUTT
		} else {
			DeltakerStatus.Type.FULLFORT
		}
	}

	private fun oppdaterStatus(status: DeltakerStatusInsert): Boolean {
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(status.deltakerId)
		if (forrigeStatus?.type == status.type && forrigeStatus.aarsak == status.aarsak) return false

		val nyStatus = DeltakerStatusInsertDbo(
			id = status.id,
			deltakerId = status.deltakerId,
			type = status.type,
			aarsak = status.aarsak,
			aarsaksbeskrivelse = status.aarsaksbeskrivelse,
			gyldigFra = status.gyldigFra ?: LocalDateTime.now()
		)

		transactionTemplate.executeWithoutResult {
			forrigeStatus?.let { deltakerStatusRepository.deaktiver(it.id) }
			deltakerStatusRepository.insert(nyStatus)
		}
		return true
	}

	private fun progressStatuser(deltakere: List<Deltaker>, sluttarsak: DeltakerStatus.Aarsak? = null) {
		val gjennomforinger = deltakere
			.map { it.gjennomforingId }
			.distinct()
			.let { gjennomforingService.getGjennomforinger(it) }

		val skalBliIkkeAktuell = deltakere.filter { it.status.harIkkeStartet() }
		val skalBliAvbrutt = deltakere
			.filter { it.status.type == DeltakerStatus.Type.DELTAR }
			.filter { sluttetForTidlig(gjennomforinger, it) }

		val skalBliHarSluttet = deltakere
			.filter { it.status.type == DeltakerStatus.Type.DELTAR }
			.filter { !deltarPaKurs(gjennomforinger, it) }

		val skalBliFullfort = deltakere
			.filter { it.status.type == DeltakerStatus.Type.DELTAR }
			.filter { deltarPaKurs(gjennomforinger, it) && !sluttetForTidlig(gjennomforinger, it) }

		oppdaterStatuser(skalBliIkkeAktuell.map { it.id }, nyStatus = DeltakerStatus.Type.IKKE_AKTUELL, sluttarsak)
		oppdaterStatuser(skalBliAvbrutt.map { it.id }, nyStatus = DeltakerStatus.Type.AVBRUTT, sluttarsak)
		oppdaterStatuser(skalBliHarSluttet.map { it.id }, nyStatus = DeltakerStatus.Type.HAR_SLUTTET, sluttarsak)
		oppdaterStatuser(skalBliFullfort.map { it.id }, nyStatus = DeltakerStatus.Type.FULLFORT, sluttarsak)
	}

	private fun deltarPaKurs(gjennomforinger: List<Gjennomforing>, deltaker: Deltaker): Boolean {
		val gjennomforing = getGjennomforing(gjennomforinger, deltaker)
		return gjennomforing.erKurs
	}

	private fun sluttetForTidlig(gjennomforinger: List<Gjennomforing>, deltaker: Deltaker): Boolean {
		val gjennomforing = getGjennomforing(gjennomforinger, deltaker)
		if (!gjennomforing.erKurs) {
			return false
		}
		gjennomforing.sluttDato?.let {
			return deltaker.sluttDato?.isBefore(it) == true
		}
		return false
	}

	private fun getGjennomforing(gjennomforinger: List<Gjennomforing>, deltaker: Deltaker): Gjennomforing {
		return gjennomforinger.find { it.id == deltaker.gjennomforingId }
			?: throw RuntimeException("Fant ikke gjennomføring med id ${deltaker.gjennomforingId}")
	}

	private fun hentStatusOrThrow(deltakerId: UUID): DeltakerStatus {
		return hentStatus(deltakerId) ?: throw NoSuchElementException("Fant ikke status på deltaker med id $deltakerId")
	}

	private fun hentStatus(deltakerId: UUID): DeltakerStatus? {
		return deltakerStatusRepository.getStatusForDeltaker(deltakerId)?.toModel() ?: return null
	}

	private fun hentAktiveStatuserForDeltakere(deltakerIder: List<UUID>): Map<UUID, DeltakerStatusDbo> {
		return deltakerStatusRepository.getAktiveStatuserForDeltakere(deltakerIder).associateBy { it.deltakerId }
	}

	private fun mapDeltakereOgAktiveStatuser(deltakere: List<DeltakerDbo>): List<Deltaker> {
		val statuser = hentAktiveStatuserForDeltakere(deltakere.map { it.id })
		return deltakere.map { d ->
			val status = statuser[d.id] ?: throw NoSuchElementException("Fant ikke status på deltaker med id ${d.id}")
			return@map d.toDeltaker(status.toModel())
		}
	}

	private fun oppdaterStatuser(
		deltakere: List<UUID>,
		nyStatus: DeltakerStatus.Type,
		sluttarsak: DeltakerStatus.Aarsak? = null
	) = deltakere
		.also { log.info("Oppdaterer status på ${it.size} deltakere") }
		.forEach {
			insertStatus(
				DeltakerStatusInsert(
					id = UUID.randomUUID(),
					deltakerId = it,
					type = nyStatus,
					aarsak = sluttarsak,
					aarsaksbeskrivelse = null,
					gyldigFra = LocalDateTime.now()
				),
				erKometDeltaker = false
			)
		}

	override fun hentDeltakerMap(deltakerIder: List<UUID>): Map<UUID, Deltaker> {
		val deltakere = deltakerRepository.getDeltakere(deltakerIder)
		return mapDeltakereOgAktiveStatuser(deltakere).associateBy { it.id }
	}

	override fun republiserAlleDeltakerePaKafka(batchSize: Int, publiserInternTopic: Boolean, publiserEksternTopic: Boolean) {
		var offset = 0

		var deltakere: List<DeltakerDbo>

		do {
			deltakere = deltakerRepository.hentDeltakere(offset, batchSize)

			val statuser = hentAktiveStatuserForDeltakere(deltakere.map { it.id })

			val gjennomforingIder = deltakere
				.map { it.gjennomforingId }.distinct()
			val erKometMasterForGjennomforingMap = gjennomforingIder
				.associateWith { unleashService.erKometMasterForTiltakstype(gjennomforingService.getGjennomforing(it).tiltak.kode) }
				.toMutableMap()

			deltakere.filterNot { erKometMasterForGjennomforingMap[it.gjennomforingId]!! }.forEach {
				val status = statuser[it.id]?.toModel()

				if (status == null) {
					log.error("Klarte ikke å republisere deltaker med id ${it.id} fordi status mangler")
					return@forEach
				}

				val deltaker = it.toDeltaker(status)

				if (publiserEksternTopic) {
					deltakerV1Producer.publiserDeltaker(deltaker, deltaker.endretDato)
				}
				if (publiserInternTopic) {
					publisherService.publish(deltaker.id, DataPublishType.DELTAKER, erKometDeltaker = false, forcePublish = true)
				}
			}

			offset += deltakere.size

			log.info("Publisert batch med deltakere på kafka, offset=$offset, batchSize=${deltakere.size}")
		} while (deltakere.isNotEmpty())

		log.info("Ferdig med republisering av deltakere på kafka")
	}

	override fun republiserDeltakerePaDeltakerV2(tiltakstype: String) {
		if (unleashService.erKometMasterForTiltakstype(tiltakstype)) {
			log.info("Republiserer ikke deltakere for tiltakstype $tiltakstype siden komet er master for tiltakstypen")
			return
		}
		val gjennomforingIder = gjennomforingService.getGjennomforingIderForTiltakstype(tiltakstype)

		gjennomforingIder.forEach {
			val deltakerePaGjennomforing = hentDeltakerePaaGjennomforing(it)
			deltakerePaGjennomforing.forEach {deltaker ->
				publisherService.publish(deltaker.id, DataPublishType.DELTAKER, erKometDeltaker = false, forcePublish = true)
			}
			log.info("Republiserte ${deltakerePaGjennomforing.size} deltakere for gjennomforingId $it")
		}
		log.info("Republiserte deltakere for ${gjennomforingIder.size} gjennomføringer på deltaker-v2")
	}

	override fun publiserDeltakerPaKafka(deltakerId: UUID, endretDato: LocalDateTime) {
		val deltaker = hentDeltaker(deltakerId) ?: error("Fant ikke deltaker med id $deltakerId")

		val tiltakstype = gjennomforingService.getGjennomforing(deltaker.gjennomforingId).tiltak.kode
		if (!unleashService.erKometMasterForTiltakstype(tiltakstype)) {
			publiser(deltaker, endretDato, false)
		}
	}

	private fun publiser(deltaker: Deltaker, endretDato: LocalDateTime, erKometMaster: Boolean) {
		if (erKometMaster) {
			log.info("Publiserer ikke deltaker som komet er master for, id ${deltaker.id}")
			return
		}
		deltakerV1Producer.publiserDeltaker(deltaker, endretDato)
		publisherService.publish(deltaker.id, DataPublishType.DELTAKER, erKometMaster)

		log.info("Publisert deltaker med id ${deltaker.id} på kafka")
	}


	override fun publiserDeltakerPaDeltakerV2Kafka(deltakerId: UUID) {
		publisherService.publish(deltakerId, DataPublishType.DELTAKER, null)
		log.info("Publisert deltaker med id $deltakerId på kafkatopic deltaker-v2")
	}

	private fun DeltakerUpsert.toUpsertDbo(brukerId: UUID) = DeltakerUpsertDbo(
		id = this.id,
		brukerId = brukerId,
		gjennomforingId = this.gjennomforingId,
		startDato = this.startDato,
		sluttDato = this.sluttDato,
		registrertDato = this.registrertDato,
		dagerPerUke = this.dagerPerUke,
		prosentStilling = this.prosentStilling,
		innsokBegrunnelse = this.innsokBegrunnelse,
		innhold = this.innhold,
		kilde = this.kilde,
		forsteVedtakFattet = this.forsteVedtakFattet,
		sistEndretAv = this.sistEndretAv,
		sistEndretAvEnhet = this.sistEndretAvEnhet
	)
}


