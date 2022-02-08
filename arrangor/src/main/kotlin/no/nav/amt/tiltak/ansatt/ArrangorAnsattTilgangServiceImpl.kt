package no.nav.amt.tiltak.ansatt

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.GjennomforingService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.*

@Service
class ArrangorAnsattTilgangServiceImpl(
	private val ansattRepository: AnsattRepository,
	private val ansattRolleRepository: AnsattRolleRepository,
	private val gjennomforingService: GjennomforingService
) : ArrangorAnsattTilgangService {

	private val secureLog = LoggerFactory.getLogger("SecureLog")

	private val personligIdentToAnsattIdCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(12))
		.maximumSize(100_000)
		.build<String, UUID>()

	private val ansattIdToArrangorIdListCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(15))
		.maximumSize(100_000)
		.build<UUID, List<UUID>>()

	private val arrangorIdToGjennomforingIdListCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(15))
		.maximumSize(100_000)
		.build<UUID, List<UUID>>()

	override fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		val harTilgang = harTilgangTilGjennomforing(ansattId, gjennomforingId)

		if (!harTilgang) {
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til gjennomføring med id=$gjennomforingId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}
	}

	override fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		val harTilgang = hentArrangorIderForAnsatt(ansattId).contains(arrangorId)

		if (!harTilgang) {
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til arrangør med id=$arrangorId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}
	}

	private fun harTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID): Boolean {
		val arrangorIder = hentArrangorIderForAnsatt(ansattId)

		arrangorIder.forEach {
			if (hentGjennomforingIderForArrangor(it).contains(gjennomforingId)) {
				return true
			}
		}

		return false
	}

	private fun hentAnsattId(ansattPersonligIdent: String): UUID {
		val ansattId = tryCacheFirstNullable(personligIdentToAnsattIdCache, ansattPersonligIdent) {
			ansattRepository.getByPersonligIdent(
				ansattPersonligIdent
			)?.id
		}

		if (ansattId == null) {
			secureLog.warn("Fnr $ansattPersonligIdent er ikke ansatt hos en tiltaksarrangør")
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		return ansattId
	}

	private fun hentArrangorIderForAnsatt(ansattId: UUID): List<UUID> {
		return tryCacheFirstNotNull(ansattIdToArrangorIdListCache, ansattId) {
			ansattRolleRepository.hentArrangorIderForAnsatt(ansattId)
		}
	}

	private fun hentGjennomforingIderForArrangor(arrangorId: UUID): List<UUID> {
		return tryCacheFirstNotNull(arrangorIdToGjennomforingIdListCache, arrangorId) {
			gjennomforingService.getGjennomforingerForArrangor(arrangorId).map { it.id }
		}
	}

	private fun <K, V> tryCacheFirstNullable(cache: Cache<K, V>, key: K, valueSupplier: () -> V?): V? {
		val value = cache.getIfPresent(key!!)

		if (value == null) {
			val newValue: V = valueSupplier.invoke() ?: return null
			cache.put(key, newValue)
			return newValue
		}

		return value
	}

	private fun <K, V> tryCacheFirstNotNull(cache: Cache<K, V>, key: K, valueSupplier: () -> V): V {
		val value = cache.getIfPresent(key!!)

		if (value == null) {
			val newValue: V = valueSupplier.invoke()
			cache.put(key, newValue)
			return newValue
		}

		return value
	}

}
