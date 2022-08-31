package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.HentKoordinatorerForGjennomforingQuery
import no.nav.amt.tiltak.utils.UpdateStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Service
class GjennomforingServiceImpl(
	private val gjennomforingRepository: GjennomforingRepository,
	private val tiltakService: TiltakService,
	private val deltakerService: DeltakerService,
	private val arrangorService: ArrangorService,
	private val transactionTemplate: TransactionTemplate,
	private val koordinatorerForGjennomforing: HentKoordinatorerForGjennomforingQuery
) : GjennomforingService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun upsert(gjennomforing: Gjennomforing): Gjennomforing {
		val storedGjennomforing = gjennomforingRepository.get(gjennomforing.id)

		if (storedGjennomforing != null) {
			return updateGjennomforing(storedGjennomforing, gjennomforing)
		}

		return gjennomforingRepository.insert(
			id = gjennomforing.id,
			tiltakId = gjennomforing.tiltak.id,
			arrangorId = gjennomforing.arrangor.id,
			navn = gjennomforing.navn,
			status = gjennomforing.status,
			startDato = gjennomforing.startDato,
			sluttDato = gjennomforing.sluttDato,
			registrertDato = gjennomforing.registrertDato,
			fremmoteDato = gjennomforing.fremmoteDato,
			navEnhetId = gjennomforing.navEnhetId,
			opprettetAar = gjennomforing.opprettetAar,
			lopenr = gjennomforing.lopenr
		).toGjennomforing(gjennomforing.tiltak, gjennomforing.arrangor)
	}

	private fun updateGjennomforing(storedGjennomforing: GjennomforingDbo, updatedGjennomforing: Gjennomforing): Gjennomforing {
		val update = storedGjennomforing.update(
			storedGjennomforing.copy(
				navn = updatedGjennomforing.navn,
				status = updatedGjennomforing.status,
				startDato = updatedGjennomforing.startDato,
				sluttDato = updatedGjennomforing.sluttDato,
				registrertDato = updatedGjennomforing.registrertDato,
				fremmoteDato = updatedGjennomforing.fremmoteDato,
				navEnhetId = updatedGjennomforing.navEnhetId,
				lopenr = updatedGjennomforing.lopenr,
				opprettetAar = updatedGjennomforing.opprettetAar
			)
		)

		return if (update.status == UpdateStatus.UPDATED) {
			gjennomforingRepository
				.update(update.updatedObject!!)
				.toGjennomforing(updatedGjennomforing.tiltak, updatedGjennomforing.arrangor)
		} else {
			storedGjennomforing
				.toGjennomforing(updatedGjennomforing.tiltak, updatedGjennomforing.arrangor)
		}
	}

	override fun slettGjennomforing(gjennomforingId: UUID) {
		transactionTemplate.execute {
			deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId).forEach {
				deltakerService.slettDeltaker(it.id)
			}

			gjennomforingRepository.delete(gjennomforingId)
		}

		log.info("Gjennomføring med id=$gjennomforingId er slettet")
	}

	override fun getGjennomforing(id: UUID): Gjennomforing {
		return gjennomforingRepository.get(id)?.let { gjennomforingDbo ->
			val tiltak = tiltakService.getTiltakById(gjennomforingDbo.tiltakId)
			val arrangor = arrangorService.getArrangorById(gjennomforingDbo.arrangorId)
			return@let gjennomforingDbo.toGjennomforing(tiltak, arrangor)
		} ?: throw NoSuchElementException("Fant ikke gjennomforing")
	}

	override fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing> {
		return gjennomforingRepository
			.get(gjennomforingIder)
			.map{ getGjennomforing(it.id) }
	}

	override fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): Set<Person> {
		return koordinatorerForGjennomforing.query(gjennomforingId)
	}

	override fun getKoordinatorerForGjennomforinger(gjennomforingIder: List<UUID>): Map<UUID, Set<Person>> {
		return koordinatorerForGjennomforing.query(gjennomforingIder)
	}

	override fun getByArrangorId(arrangorId: UUID): List<Gjennomforing> {
		return gjennomforingRepository.getByArrangorId(arrangorId).map {
			val tiltak = tiltakService.getTiltakById(it.tiltakId)
			val arrangor = arrangorService.getArrangorById(it.arrangorId)
			return@map it.toGjennomforing(tiltak, arrangor)
		}
	}

}
