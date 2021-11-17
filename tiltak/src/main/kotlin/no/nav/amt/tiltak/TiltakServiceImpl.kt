package no.nav.amt.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.deltaker.DeltakerService
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
	private val tiltaksinstansRepository: TiltaksinstansRepository,
	private val deltakerService: DeltakerService
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
		val storedTiltaksinstans = tiltaksinstansRepository.getByArenaId(arenaId)

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
				tiltaksinstansRepository.update(update.updatedObject!!).toTiltaksinstans()
			} else {
				storedTiltaksinstans.toTiltaksinstans()
			}
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

	override fun upsertDeltaker(
		tiltaksgjennomforing: UUID,
		fodselsnummer: String,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status,
		arenaStatus: String?,
		dagerPerUke: Int?,
		prosentStilling: Float?
	): Deltaker {
		return deltakerService.addUpdateDeltaker(
			tiltaksinstans = tiltaksgjennomforing,
			fodselsnummer = fodselsnummer,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			status = status,
			arenaStatus = arenaStatus,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling
		)
	}

}
