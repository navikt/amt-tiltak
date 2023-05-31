package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.domain.tiltak.STATUSER_SOM_KAN_SKJULES
import no.nav.amt.tiltak.core.domain.tiltak.harIkkeStartet
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerUpdateDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.SkjultDeltakerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.UUID

@Service
open class DeltakerServiceImpl(
	private val deltakerRepository: DeltakerRepository,
	private val deltakerStatusRepository: DeltakerStatusRepository,
	private val brukerService: BrukerService,
	private val endringsmeldingService: EndringsmeldingService,
	private val skjultDeltakerRepository: SkjultDeltakerRepository,
	private val gjennomforingService: GjennomforingService,
	private val transactionTemplate: TransactionTemplate,
	private val kafkaProducerService: KafkaProducerService,
	private val publisherService: DataPublisherService
) : DeltakerService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsertDeltaker(personIdent: String, deltaker: DeltakerUpsert) {
		val lagretDeltaker = hentDeltaker(deltaker.id)

		transactionTemplate.executeWithoutResult {
			if (lagretDeltaker == null) {
				insertDeltaker(personIdent, deltaker)
			} else if(!deltaker.compareTo(lagretDeltaker)){
				update(deltaker)
			}

			oppdaterStatus(deltaker.statusInsert)

			val oppdatertDeltaker = hentDeltaker(deltaker.id)
				?: throw IllegalStateException("Fant ikke deltaker med id ${deltaker.id}")

			kafkaProducerService.publiserDeltaker(oppdatertDeltaker)
			publisherService.publish(oppdatertDeltaker.id, DataPublishType.DELTAKER)
			publisherService.publish(oppdatertDeltaker.gjennomforingId, DataPublishType.DELTAKERLISTE)
		}

	}

	override fun insertStatus(status: DeltakerStatusInsert) {
		transactionTemplate.executeWithoutResult {
			oppdaterStatus(status)

			val oppdatertDeltaker = hentDeltaker(status.deltakerId)
				?: throw IllegalStateException("Fant ikke deltaker med id ${status.deltakerId}")

			kafkaProducerService.publiserDeltaker(oppdatertDeltaker)
			publisherService.publish(oppdatertDeltaker.id, DataPublishType.DELTAKER)
			publisherService.publish(oppdatertDeltaker.gjennomforingId, DataPublishType.DELTAKERLISTE)
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

	override fun hentDeltakereMedPersonIdent(personIdent: String): List<Deltaker>{
		val deltakere =  deltakerRepository.getDeltakereMedPersonIdent(personIdent)
		return mapDeltakereOgAktiveStatuser(deltakere)
	}

	override fun progressStatuser() {

		val deltakere = deltakerRepository.erPaaAvsluttetGjennomforing()
			.plus (deltakerRepository.sluttDatoPassert())
			.let { mapDeltakereOgAktiveStatuser(it) }

		progressStatuser(deltakere)
		oppdaterStatuser(deltakerRepository.skalHaStatusDeltar().map { it.id }, nyStatus = DeltakerStatus.Type.DELTAR)

		deltakere.forEach { deltaker ->
			publisherService.publish(deltaker.id, DataPublishType.DELTAKER)
			publisherService.publish(deltaker.gjennomforingId, DataPublishType.DELTAKERLISTE)
		}
	}

	override fun slettDeltakerePaaGjennomforing(gjennomforingId: UUID) {
		hentDeltakerePaaGjennomforing(gjennomforingId).forEach {
			slettDeltaker(it.id)
		}
	}

	override fun slettDeltaker(deltakerId: UUID) {
		transactionTemplate.execute {
			endringsmeldingService.slett(deltakerId)
			deltakerStatusRepository.slett(deltakerId)
			skjultDeltakerRepository.slett(deltakerId)
			deltakerRepository.slettVeilederrelasjonOgDeltaker(deltakerId)
			kafkaProducerService.publiserSlettDeltaker(deltakerId)
		}

		log.info("Deltaker med id=$deltakerId er slettet")
		publisherService.publish(deltakerId, DataPublishType.DELTAKER)
	}

	override fun oppdaterNavEnhet(personIdent: String, navEnhet: NavEnhet?) {
		brukerService.oppdaterNavEnhet(personIdent, navEnhet)

	}

	override fun erSkjermet(deltakerId: UUID) : Boolean {
		val deltaker = hentDeltaker(deltakerId)?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")
		return deltaker.erSkjermet
	}

	override fun settSkjermet(personIdent: String, erSkjermet: Boolean) {
		brukerService.settErSkjermet(personIdent, erSkjermet)
	}

	override fun hentBrukerInfo(deltakerId: UUID): Map<String, UUID?>  {
		val navEnhetId = deltakerRepository.get(deltakerId)?.navEnhet?.id
		return mapOf(
			"brukerId" to deltakerRepository.hentBrukerId(deltakerId),
			"navEnhetId" to navEnhetId,
		)
	}

	private fun update(deltaker: DeltakerUpsert) {
		val toUpdate = DeltakerUpdateDbo(
			id = deltaker.id,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			registrertDato = deltaker.registrertDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			innsokBegrunnelse = deltaker.innsokBegrunnelse
		)

		deltakerRepository.update(toUpdate)
		publisherService.publish(deltaker.id, DataPublishType.DELTAKER)
		publisherService.publish(deltaker.gjennomforingId, DataPublishType.DELTAKERLISTE)
	}

	private fun oppdaterStatus(status: DeltakerStatusInsert) {
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(status.deltakerId)
		if (forrigeStatus?.type == status.type && forrigeStatus.aarsak == status.aarsak) return

		val nyStatus = DeltakerStatusInsertDbo(
			id = status.id,
			deltakerId = status.deltakerId,
			type = status.type,
			aarsak = status.aarsak,
			gyldigFra = status.gyldigFra?: LocalDateTime.now()
		)

		transactionTemplate.executeWithoutResult {
			forrigeStatus?.let { deltakerStatusRepository.deaktiver(it.id) }
			deltakerStatusRepository.insert(nyStatus)
		}

		publisherService.publish(status.deltakerId, DataPublishType.DELTAKER)
	}

	private fun progressStatuser(deltakere: List<Deltaker>) {
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
			.filter { !sluttetForTidlig(gjennomforinger, it) }

		oppdaterStatuser(skalBliIkkeAktuell.map { it.id }, nyStatus = DeltakerStatus.Type.IKKE_AKTUELL)
		oppdaterStatuser(skalBliAvbrutt.map { it.id }, nyStatus = DeltakerStatus.Type.AVBRUTT)
		oppdaterStatuser(skalBliHarSluttet.map { it.id }, nyStatus = DeltakerStatus.Type.HAR_SLUTTET)
	}

	private fun sluttetForTidlig(gjennomforinger: List<Gjennomforing>, deltaker: Deltaker): Boolean {
		val gjennomforing = gjennomforinger.find { it.id == deltaker.gjennomforingId } ?: throw RuntimeException("Fant ikke gjennomføring med id ${deltaker.gjennomforingId}")
		if (!gjennomforing.erKurs) {
			return false
		}
		gjennomforing.sluttDato?.let {
			return deltaker.sluttDato?.isBefore(it) == true
		}
		return false
	}

	private fun insertDeltaker(fodselsnummer: String, deltaker: DeltakerUpsert) {
		val brukerId = brukerService.getOrCreate(fodselsnummer)
		val toInsert = DeltakerInsertDbo(
			id = deltaker.id,
			brukerId = brukerId,
			gjennomforingId = deltaker.gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			registrertDato = deltaker.registrertDato,
			innsokBegrunnelse = deltaker.innsokBegrunnelse
		)

		deltakerRepository.insert(toInsert)
		publisherService.publish(toInsert.id, DataPublishType.DELTAKER)
		publisherService.publish(toInsert.gjennomforingId, DataPublishType.DELTAKERLISTE)

	}

	private fun hentStatusOrThrow(deltakerId: UUID) : DeltakerStatus {
		return hentStatus(deltakerId) ?: throw NoSuchElementException("Fant ikke status på deltaker med id $deltakerId")
	}

	private fun hentStatus(deltakerId: UUID) : DeltakerStatus? {
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

	private fun oppdaterStatuser(deltakere: List<UUID>, nyStatus: DeltakerStatus.Type) = deltakere
		.also { log.info("Oppdaterer status på ${it.size} deltakere") }
		.forEach {
			insertStatus(DeltakerStatusInsert(
				id = UUID.randomUUID(),
				deltakerId = it,
				type = nyStatus,
				aarsak = null,
				gyldigFra = LocalDateTime.now()
			))
		}

	override fun finnesBruker(personIdent: String): Boolean {
		return brukerService.finnesBruker(personIdent)
	}

	override fun oppdaterAnsvarligVeileder(personIdent: String, navAnsattId: UUID) {
		brukerService.oppdaterAnsvarligVeileder(personIdent, navAnsattId)
	}

	override fun hentDeltakerMap(deltakerIder: List<UUID>): Map<UUID, Deltaker> {
		val deltakere = deltakerRepository.getDeltakere(deltakerIder)
		return mapDeltakereOgAktiveStatuser(deltakere).associateBy { it.id }
	}

	override fun kanDeltakerSkjulesForTiltaksarrangor(deltakerId: UUID): Boolean {
		val deltakerStatus = hentStatusOrThrow(deltakerId)

		return STATUSER_SOM_KAN_SKJULES.contains(deltakerStatus.type)
	}

	override fun skjulDeltakerForTiltaksarrangor(deltakerId: UUID, arrangorAnsattId: UUID) {
		if (!kanDeltakerSkjulesForTiltaksarrangor(deltakerId))
			throw IllegalStateException("Kan ikke skjule deltaker $deltakerId. Ugyldig status")

		skjultDeltakerRepository.skjulDeltaker(UUID.randomUUID(), deltakerId, arrangorAnsattId)
	}

	override fun opphevSkjulDeltakerForTiltaksarrangor(deltakerId: UUID) {
		skjultDeltakerRepository.opphevSkjulDeltaker(deltakerId)
		publisherService.publish(deltakerId, DataPublishType.DELTAKER)
	}

	override fun erSkjultForTiltaksarrangor(deltakerId: UUID): Boolean {
		return skjultDeltakerRepository.erSkjultForTiltaksarrangor(listOf(deltakerId)).getOrDefault(deltakerId, false)
	}

	override fun erSkjultForTiltaksarrangor(deltakerIder: List<UUID>): Map<UUID, Boolean> {
		return skjultDeltakerRepository.erSkjultForTiltaksarrangor(deltakerIder)
	}

	override fun republiserAlleDeltakerePaKafka(batchSize: Int) {
		var offset = 0

		var deltakere: List<DeltakerDbo>

		do {
			deltakere = deltakerRepository.hentDeltakere(offset, batchSize)

			val statuser = hentAktiveStatuserForDeltakere(deltakere.map { it.id })

			deltakere.forEach {
				val status = statuser[it.id]?.toModel()

				if (status == null) {
					log.error("Klarte ikke å republisere deltaker med id ${it.id} fordi status mangler")
					return@forEach
				}

				val deltaker = it.toDeltaker(status)

				kafkaProducerService.publiserDeltaker(deltaker)
				publisherService.publish(deltaker.id, DataPublishType.DELTAKER)
				publisherService.publish(deltaker.gjennomforingId, DataPublishType.DELTAKERLISTE)
			}

			offset += deltakere.size

			log.info("Publisert batch med deltakere på kafka, offset=$offset, batchSize=${deltakere.size}")
		} while (deltakere.isNotEmpty())

		log.info("Ferdig med republisering av deltakere på kafka")
	}

}


