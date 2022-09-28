package no.nav.amt.tiltak.person

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClient
import no.nav.amt.tiltak.common.utils.CacheUtils
import no.nav.amt.tiltak.core.port.SkjermetPersonService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class SkjermetPersonServiceImpl(
	private val poaoTilgangClient: PoaoTilgangClient
) : SkjermetPersonService {

	private val norskIdentToErSkjermetCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.maximumSize(10_000)
		.build<String, Boolean>()

	override fun erSkjermet(norskIdent: String): Boolean {
		return CacheUtils.tryCacheFirstNotNull(norskIdentToErSkjermetCache, norskIdent) {
			poaoTilgangClient.erSkjermet(listOf(norskIdent)).getOrElse(norskIdent) {
				secureLog.warn("Mangler data for skjermet person med fnr=$norskIdent, defaulter til true")
				return@getOrElse true
			}
		}
	}

}
