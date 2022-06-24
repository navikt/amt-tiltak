package no.nav.amt.tiltak.tilgangskontroll.altinn

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils
import no.nav.amt_tiltak.clients.amt_altinn_acl.AmtAltinnAclClient
import no.nav.amt_tiltak.clients.amt_altinn_acl.Rettighet
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AltinnService(
	private val amtAltinnAclClient: AmtAltinnAclClient
) {

	private val personligIdentToAltinnRettigheterCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, List<Rettighet>>()

	@Value("\${altinn-koordinator-rettighetid}")
	lateinit var altinnKoordinatorRettighetId: String

	fun hentVirksomehterMedKoordinatorRettighet(ansattPersonligIdent: String): List<String> {
		return hentAlleRettigheter(ansattPersonligIdent)
			.filter { it.id == altinnKoordinatorRettighetId }
			.map { it.organisasjonsnummer }
	}

	private fun hentAlleRettigheter(ansattPersonligIdent: String): List<Rettighet> {
		return CacheUtils.tryCacheFirstNotNull(personligIdentToAltinnRettigheterCache, ansattPersonligIdent) {
			return@tryCacheFirstNotNull amtAltinnAclClient.hentRettigheter(
				ansattPersonligIdent,
				alleRettigheter()
			)
		}
	}

	private fun alleRettigheter(): List<String> {
		return listOf(altinnKoordinatorRettighetId)
	}

}
