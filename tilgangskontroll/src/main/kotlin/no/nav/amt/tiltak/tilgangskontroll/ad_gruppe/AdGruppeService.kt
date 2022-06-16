package no.nav.amt.tiltak.tilgangskontroll.ad_gruppe

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.poao_tilgang.AdGruppe
import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClient
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils.tryCacheFirstNotNull
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
open class AdGruppeService(
	private val poaoTilgangClient: PoaoTilgangClient
)  {

	private val brukerAzureIdToAdGruppeCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.maximumSize(10_000)
		.build<UUID, List<AdGruppe>>()

	open fun hentAdGrupper(navAnsattAzureId: UUID): List<AdGruppe> {
		return tryCacheFirstNotNull(brukerAzureIdToAdGruppeCache, navAnsattAzureId) {
			poaoTilgangClient.hentAdGrupper(navAnsattAzureId)
		}
	}

	open fun erMedlemAvGruppe(navAnsattAzureId: UUID, adGruppeNavn: String): Boolean {
		return hentAdGrupper(navAnsattAzureId).any { it.name == adGruppeNavn }
	}

}
