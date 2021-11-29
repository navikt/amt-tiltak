package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.util.*

@Service
open class TiltakServiceImpl(
	private val tiltakRepository: TiltakRepository
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

	override fun getTiltakById(id: UUID): Tiltak {
		return tiltakRepository.getAll()
			.find { it.id == id }?.toTiltak()
			?: throw IllegalStateException("Tiltak med id $id existerer ikke")
	}

}
