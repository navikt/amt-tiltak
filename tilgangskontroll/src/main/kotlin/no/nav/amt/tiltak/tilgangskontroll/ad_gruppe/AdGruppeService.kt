package no.nav.amt.tiltak.tilgangskontroll.ad_gruppe

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.poao_tilgang.AdGruppe
import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClient
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils.tryCacheFirstNotNull
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AdGruppeService(
	private val poaoTilgangClient: PoaoTilgangClient
)  {

	private val navIdentToAdGruppeCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.maximumSize(10_000)
		.build<String, List<AdGruppe>>()

	fun hentAdGrupper(navIdent: String): List<AdGruppe> {
		return tryCacheFirstNotNull(navIdentToAdGruppeCache, navIdent) {
			poaoTilgangClient.hentAdGrupper(navIdent)
		}
	}

	fun erMedlemAvGruppe(navIdent: String, adGruppeNavn: String): Boolean {
		return hentAdGrupper(navIdent).any { it.name == adGruppeNavn }
	}

}
