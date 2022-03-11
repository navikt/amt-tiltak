package no.nav.amt.tiltak.ansatt

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.utils.CacheUtils.tryCacheFirstNotNull
import no.nav.amt.tiltak.utils.CacheUtils.tryCacheFirstNullable
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
	private val gjennomforingTilgangRepository: GjennomforingTilgangRepository,
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

	private val ansattIdToGjennomforingIdListCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(5))
		.maximumSize(100_000)
		.build<UUID, List<UUID>>()

	override fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		val gjennomforinger = hentGjennomforingerForAnsatt(ansattId)

		val harTilgang = gjennomforinger.contains(gjennomforingId)

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

	override fun hentGjennomforingIder(ansattPersonligIdent: String): List<UUID> {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		return hentGjennomforingerForAnsatt(ansattId)
	}

	private fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return tryCacheFirstNotNull(ansattIdToGjennomforingIdListCache, ansattId) {
			gjennomforingTilgangRepository.hentGjennomforingerForAnsatt(ansattId)
		}
	}

}
