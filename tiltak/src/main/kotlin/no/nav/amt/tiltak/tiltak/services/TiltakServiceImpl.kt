package no.nav.amt.tiltak.tiltak.services

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
open class TiltakServiceImpl(
	private val tiltakRepository: TiltakRepository
) : TiltakService {

	private val tiltakCache = Caffeine.newBuilder()
		.expireAfterWrite(1, TimeUnit.HOURS)
		.maximumSize(150)
		.build<UUID, TiltakDbo>()

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

	override fun upsertTiltak(id: UUID, navn: String, kode: String): Tiltak {
		TODO("Not yet implemented")
	}

	override fun getTiltakFromArenaId(arenaId: String): Tiltak? {
		return tiltakRepository.getByArenaId(arenaId)?.toTiltak()
	}

	override fun getTiltakById(id: UUID): Tiltak {
		return getTiltak(id).toTiltak()
	}

	private fun getTiltak(id: UUID): TiltakDbo {
		val tiltak = tiltakCache.getIfPresent(id)

		if (tiltak != null) {
			return tiltak
		}

		tiltakRepository.getAll().forEach { tiltakCache.put(it.id, it) }

		return tiltakCache.getIfPresent(id) ?: throw IllegalStateException("Tiltak med id $id existerer ikke")
	}

}
