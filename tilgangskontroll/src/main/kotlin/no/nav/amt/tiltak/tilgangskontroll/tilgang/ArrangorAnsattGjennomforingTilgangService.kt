package no.nav.amt.tiltak.tilgangskontroll.tilgang

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattGjennomforingTilgang
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

@Service
open class ArrangorAnsattGjennomforingTilgangService(
	private val arrangorAnsattGjennomforingTilgangRepository: ArrangorAnsattGjennomforingTilgangRepository
) {

	private val defaultGyldigTil = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	private val ansattIdToGjennomforingIdListCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(5))
		.maximumSize(100_000)
		.build<UUID, List<UUID>>()

	open fun opprettTilgang(id: UUID, arrangorAnsattId: UUID, gjennomforingId: UUID) {
		arrangorAnsattGjennomforingTilgangRepository.opprettTilgang(
			id = id,
			arrangorAnsattId = arrangorAnsattId,
			gjennomforingId = gjennomforingId,
			gyldigFra = ZonedDateTime.now(),
			gyldigTil = defaultGyldigTil
		)

		ansattIdToGjennomforingIdListCache.invalidate(arrangorAnsattId)
	}

	open fun hentTilgang(id: UUID): ArrangorAnsattGjennomforingTilgang {
		return mapGjennomforingTilgang(arrangorAnsattGjennomforingTilgangRepository.get(id))
	}

	open fun stopTilgang(id: UUID) {
		arrangorAnsattGjennomforingTilgangRepository.oppdaterGyldigTil(id, ZonedDateTime.now())
		// Should invalidate cache
	}

	fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return CacheUtils.tryCacheFirstNotNull(ansattIdToGjennomforingIdListCache, ansattId) {
			arrangorAnsattGjennomforingTilgangRepository.hentAktiveGjennomforingTilgangerForAnsatt(ansattId)
				.map { it.gjennomforingId }
		}
	}

	private fun mapGjennomforingTilgang(dbo: ArrangorAnsattGjennomforingTilgangDbo): ArrangorAnsattGjennomforingTilgang {
		return ArrangorAnsattGjennomforingTilgang(
			id = dbo.id,
			ansattId = dbo.ansattId,
			gjennomforingId = dbo.gjennomforingId,
			createdAt = dbo.createdAt
		)
	}

}
