package no.nav.amt.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltaksinstansRepository
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class TiltakServiceImpl(
	private val tiltakRepository: TiltakRepository,
	private val tiltaksinstansRepository: TiltaksinstansRepository
) : TiltakService {

	override fun addUpdateTiltak(arenaId: String, navn: String, kode: String): Tiltak {
		val storedTiltak = tiltakRepository.getByArenaId(arenaId)

		if (storedTiltak != null) {
			val update = storedTiltak.update(
				storedTiltak.copy(
					navn = navn,
					type = kode
				)
			)

			if(update.status == UpdateStatus.UPDATED) {
				tiltakRepository.update(update.updatedObject!!)
			}

			throw NotImplementedError("Update is not yet implemented") //TODO
		}

		return tiltakRepository.insert(arenaId, navn, kode).toTiltak()
	}

	override fun getTiltakFromArenaId(arenaId: String): Tiltak? {
		return tiltakRepository.getByArenaId(arenaId)?.toTiltak()
	}

	override fun addUpdateTiltaksinstans(
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
		val storedTiltaksinstans = tiltaksinstansRepository.getByArenaId(arenaId)

		if (storedTiltaksinstans != null) {
			throw NotImplementedError("Update is not yet implemented") //TODO
		}

		return tiltaksinstansRepository.insert(
			arenaId = arenaId,
			tiltakId = tiltakId,
			tiltaksleverandorId = tiltaksleverandorId,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		).toTiltaksinstans()
	}

	override fun getTiltaksinstansFromArenaId(arenaId: Int): TiltakInstans? {
		return tiltaksinstansRepository.getByArenaId(arenaId)?.toTiltaksinstans()
	}


}
