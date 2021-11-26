package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakInstansRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class TiltakServiceImpl(
	private val tiltakRepository: TiltakRepository,
	private val tiltakInstansRepository: TiltakInstansRepository
) : TiltakService {

	override fun upsertTiltak(arenaId: String, navn: String, kode: String): Tiltak {
		val storedTiltak = tiltakRepository.getByArenaId(arenaId)

		if (storedTiltak != null) {
			val update = storedTiltak.update(
				storedTiltak.copy(
					navn = navn,
					type = kode
				)
			)

			return if (update.status == UpdateStatus.UPDATED) {
				tiltakRepository.update(update.updatedObject!!).toTiltak()
			} else {
				storedTiltak.toTiltak()
			}
		}

		return tiltakRepository.insert(arenaId, navn, kode).toTiltak()
	}

	override fun getTiltakFromArenaId(arenaId: String): Tiltak? {
		return tiltakRepository.getByArenaId(arenaId)?.toTiltak()
	}

	override fun upsertTiltaksinstans(
		arenaId: Int,
		tiltakId: UUID,
		tiltaksleverandorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltakInstans {
		val storedTiltaksinstans = tiltakInstansRepository.getByArenaId(arenaId)
		val tiltak = getTiltakOrElseThrow(tiltakId)

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
			tiltaksleverandorId = tiltaksleverandorId,
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
			val tiltak = getTiltakOrElseThrow(instans.tiltakId)
			return instans.toTiltakInstans(tiltak)
		} ?: throw NoSuchElementException("Fant ikke tiltakInstans")
	}

	override fun getTiltakInstans(id: UUID): TiltakInstans {
		return tiltakInstansRepository.get(id)?.let { instans ->
			val tiltak = getTiltakOrElseThrow(instans.tiltakId)
			return instans.toTiltakInstans(tiltak)
		} ?: throw NoSuchElementException("Fant ikke tiltakInstans")
	}

	private fun getTiltakOrElseThrow(id: UUID): Tiltak {
		return tiltakRepository.get(id)?.toTiltak()?: throw IllegalStateException("Fant ikke tiltak")
	}

}
