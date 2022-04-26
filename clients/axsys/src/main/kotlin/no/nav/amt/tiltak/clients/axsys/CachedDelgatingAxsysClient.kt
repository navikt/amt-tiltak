package no.nav.amt.tiltak.clients.axsys

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

private typealias NavIdent = String

class CachedDelgatingAxsysClient(private val delegate: AxsysClient) : AxsysClient {

	private val cache = Caffeine.newBuilder()
		.expireAfterAccess(Duration.ofMinutes(5))
		.build<NavIdent, List<EnhetTilgang>> {
				navIdent -> delegate.hentTilganger(navIdent)
		}

	override fun hentTilganger(navIdent: String) = cache.get(navIdent) ?:
		throw IllegalStateException("Fant ikke brukerident")

}
