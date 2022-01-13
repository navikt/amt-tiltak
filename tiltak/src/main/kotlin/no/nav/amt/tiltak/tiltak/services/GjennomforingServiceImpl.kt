package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class GjennomforingServiceImpl(
	private val gjennomforingRepository: GjennomforingRepository,
	private val tiltakService: TiltakService
) : GjennomforingService {

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

	override fun getGjennomforing(id: UUID): Gjennomforing {
		return gjennomforingRepository.get(id)?.let { gjennomforingDbo ->
			val tiltak = tiltakService.getTiltakById(gjennomforingDbo.tiltakId)
			return gjennomforingDbo.toGjennomforing(tiltak)
		} ?: throw NoSuchElementException("Fant ikke gjennomforing")
	}

	override fun getGjennomforingerForArrangor(arrangorId: UUID): List<Gjennomforing> {
		val gjennomforing = gjennomforingRepository.getByArrandorId(arrangorId)

		return gjennomforing.map { gjennomforingDbo ->
			gjennomforingDbo.toGjennomforing(tiltakService.getTiltakById(gjennomforingDbo.tiltakId))
		}
	}
}
