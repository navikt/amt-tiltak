package no.nav.amt.tiltak.tiltak.services

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
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

	override fun upsertTiltak(id: UUID, navn: String, kode: String): Tiltak {
		val storedTiltak = getNullableTiltakById(id)

		// Dette kan forenkles med upsert
		return if (storedTiltak != null) {
			tiltakRepository.update(id, navn, kode)
		} else {
			tiltakRepository.insert(id, navn, kode)
		}.toTiltak()
	}

	override fun getTiltakById(id: UUID): Tiltak {
		return getNullableTiltakById(id) ?: throw IllegalStateException("Tiltak med id $id existerer ikke")
	}

	private fun getNullableTiltakById(id: UUID): Tiltak? {
		val tiltak = tiltakCache.getIfPresent(id)

		if (tiltak != null) {
			return tiltak.toTiltak()
		}

		tiltakRepository.getAll().forEach { tiltakCache.put(it.id, it) }

		return tiltakCache.getIfPresent(id)?.toTiltak()
	}

}
