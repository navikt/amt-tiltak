package no.nav.amt.tiltak.clients.axsys

import com.github.benmanes.caffeine.cache.Caffeine
import java.lang.IllegalStateException
import java.time.Duration

private typealias Brukerident = String

class CachedDelgatingAxsysClient(private val delegate: AxsysClient) : AxsysClient {

	val cache = Caffeine.newBuilder()
		.expireAfterAccess(Duration.ofMinutes(5))
		.build<Brukerident, Enheter> {
				brukerident -> delegate.hentTilganger(brukerident)
		}

	override fun hentTilganger(brukerident: String) = cache.get(brukerident) ?:
		throw IllegalStateException("Fant ikke brukerident")

}
