package no.nav.amt.tiltak.tilgangskontroll.altinn

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.amt_altinn_acl.AmtAltinnAclClient
import no.nav.amt.tiltak.clients.amt_altinn_acl.AltinnRettighet
import no.nav.amt.tiltak.tilgangskontroll.tilgang.AnsattRolle
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.IllegalArgumentException

@Service
class AltinnService(
	@Value("\${altinn-koordinator-rettighetid}") private val altinnKoordinatorRettighetId: String,
	private val amtAltinnAclClient: AmtAltinnAclClient
) {

	private val alleRettigheter = listOf(altinnKoordinatorRettighetId)

	private val personligIdentToAltinnRettigheterCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, List<AltinnRettighet>>()

	fun hentVirksomheterMedKoordinatorRettighet(ansattPersonligIdent: String): List<String> {
		return hentAlleRettigheter(ansattPersonligIdent)
			.filter { it.id == altinnKoordinatorRettighetId }
			.map { it.organisasjonsnummer }
	}

	fun hentAltinnRettigheter(ansattPersonligIdent: String): List<Rettighet> {
		return hentAlleRettigheter(ansattPersonligIdent)
			.map { Rettighet(mapRettighetIdTilRolle(it.id), it.organisasjonsnummer) }
	}

	private fun mapRettighetIdTilRolle(altinnRettighet: String): AnsattRolle {
		return when (altinnRettighet) {
			altinnKoordinatorRettighetId -> AnsattRolle.KOORDINATOR
			else -> throw IllegalArgumentException("Kan ikke mappe altinnrettighet $altinnRettighet")
		}
	}

	private fun hentAlleRettigheter(ansattPersonligIdent: String): List<AltinnRettighet> {
		return CacheUtils.tryCacheFirstNotNull(personligIdentToAltinnRettigheterCache, ansattPersonligIdent) {
			return@tryCacheFirstNotNull amtAltinnAclClient.hentRettigheter(
				ansattPersonligIdent,
				alleRettigheter
			)
		}
	}
}

data class Rettighet(
	val rolle: AnsattRolle,
	val organisasjonsnummer: String
)
