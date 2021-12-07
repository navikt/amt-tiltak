package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakInstansRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class TiltakInstansServiceImpl(
	private val tiltakInstansRepository: TiltakInstansRepository,
	private val tiltakService: TiltakService
) : TiltakInstansService {

	override fun upsertTiltaksinstans(
		arenaId: Int,
		tiltakId: UUID,
		arrangorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltakInstans {
		val storedTiltaksinstans = tiltakInstansRepository.getByArenaId(arenaId)
		val tiltak = tiltakService.getTiltakById(tiltakId)

		if (storedTiltaksinstans != null) {
			val update = storedTiltaksinstans.update(
				storedTiltaksinstans.copy(
					navn = navn,
					status = status,
					oppstartDato = oppstartDato,
					sluttDato = sluttDato,
					registrertDato = registrertDato,
					fremmoteDato = fremmoteDato
				)
			)

			return if (update.status == UpdateStatus.UPDATED) {
				tiltakInstansRepository.update(update.updatedObject!!).toTiltakInstans(tiltak)
			} else {
				storedTiltaksinstans.toTiltakInstans(tiltak)
			}
		}

		return tiltakInstansRepository.insert(
			arenaId = arenaId,
			tiltakId = tiltak.id,
			arrangorId = arrangorId,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		).toTiltakInstans(tiltak)
	}

	override fun getTiltaksinstansFromArenaId(arenaId: Int): TiltakInstans {
		return tiltakInstansRepository.getByArenaId(arenaId)?.let { instans ->
			val tiltak = tiltakService.getTiltakById(instans.tiltakId)
			return instans.toTiltakInstans(tiltak)
		} ?: throw NoSuchElementException("Fant ikke tiltakInstans")
	}

	override fun getTiltakInstans(id: UUID): TiltakInstans {
		return tiltakInstansRepository.get(id)?.let { instans ->
			val tiltak = tiltakService.getTiltakById(instans.tiltakId)
			return instans.toTiltakInstans(tiltak)
		} ?: throw NoSuchElementException("Fant ikke tiltakInstans")
	}

	override fun getTiltakInstanserForArrangor(arrangorId: UUID): List<TiltakInstans> {
		val instansDbos = tiltakInstansRepository.getByArrandorId(arrangorId)

		return instansDbos.map { instans ->
			instans.toTiltakInstans(tiltakService.getTiltakById(instans.tiltakId))
		}
	}
}
