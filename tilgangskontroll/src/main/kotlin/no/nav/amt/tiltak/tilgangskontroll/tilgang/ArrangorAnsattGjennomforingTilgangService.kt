package no.nav.amt.tiltak.tilgangskontroll.tilgang

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattGjennomforingTilgang
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

@Service
open class ArrangorAnsattGjennomforingTilgangService(
	private val arrangorAnsattGjennomforingTilgangRepository: ArrangorAnsattGjennomforingTilgangRepository,
	private val transactionTemplate: TransactionTemplate
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

	open fun fjernTilgang(arrangorAnsattId: UUID, gjennomforingId: UUID) {
		val tilganger = arrangorAnsattGjennomforingTilgangRepository
			.hentAktiveGjennomforingTilgangerForAnsatt(arrangorAnsattId)

		val gyldigTil = ZonedDateTime.now()

		transactionTemplate.executeWithoutResult {
			tilganger
				.filter { it.gjennomforingId == gjennomforingId }
				.forEach { arrangorAnsattGjennomforingTilgangRepository.oppdaterGyldigTil(it.id, gyldigTil)  }
		}

		ansattIdToGjennomforingIdListCache.invalidate(arrangorAnsattId)
	}

	fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return CacheUtils.tryCacheFirstNotNull(ansattIdToGjennomforingIdListCache, ansattId) {
			arrangorAnsattGjennomforingTilgangRepository.hentAktiveGjennomforingTilgangerForAnsatt(ansattId)
				.map { it.gjennomforingId }
		}
	}

}
