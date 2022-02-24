package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class GjennomforingServiceImpl(
	private val gjennomforingRepository: GjennomforingRepository,
	private val tiltakService: TiltakService,
	private val deltakerService: DeltakerService,
	private val transactionTemplate: TransactionTemplate
) : GjennomforingService {

	private val log = LoggerFactory.getLogger(this::class.java)

	override fun upsertGjennomforing(
		id: UUID,
		tiltakId: UUID,
		arrangorId: UUID,
		navn: String,
		status: Gjennomforing.Status,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime,
		fremmoteDato: LocalDateTime?
	): Gjennomforing {
		val storedGjennomforing = gjennomforingRepository.get(id)

		val tiltak = tiltakService.getTiltakById(tiltakId)

		if (storedGjennomforing != null) {
			val update = storedGjennomforing.update(
				storedGjennomforing.copy(
					navn = navn,
					status = status,
					startDato = startDato,
					sluttDato = sluttDato,
					registrertDato = registrertDato,
					fremmoteDato = fremmoteDato
				)
			)

			return if (update.status == UpdateStatus.UPDATED) {
				gjennomforingRepository.update(update.updatedObject!!).toGjennomforing(tiltak)
			} else {
				storedGjennomforing.toGjennomforing(tiltak)
			}
		}

		return gjennomforingRepository.insert(
			id = id,
			tiltakId = tiltak.id,
			arrangorId = arrangorId,
			navn = navn,
			status = status,
			startDato = startDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		).toGjennomforing(tiltak)
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
			return gjennomforingDbo.toGjennomforing(tiltak)
		} ?: throw NoSuchElementException("Fant ikke gjennomforing")
	}

	override fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing> {
		val gjennomforinger = gjennomforingRepository.get(gjennomforingIder)

		return gjennomforinger.map { gjennomforingDbo ->
			gjennomforingDbo.toGjennomforing(tiltakService.getTiltakById(gjennomforingDbo.tiltakId))
		}
	}

}
