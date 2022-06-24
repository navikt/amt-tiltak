package no.nav.amt.tiltak.clients.poao_tilgang

import java.util.*

interface PoaoTilgangClient {

	fun hentAdGrupper(navAnsattAzureId: UUID): List<AdGruppe>

	fun erSkjermet(norskeIdenter: List<String>): Map<String, Boolean>

}

data class AdGruppe(
	val id: UUID,
	val name: String
)
