package no.nav.amt.tiltak.clients.mulighetsrommet_api_client

import java.util.*

interface MulighetsrommetApiClient {

	fun hentGjennomforingArenaData(id: UUID): GjennomforingArenaData

}

data class GjennomforingArenaData(
	val opprettetAar: Int,
	val lopenr: Int,
	val virksomhetsnummer: String?,
	val ansvarligNavEnhetId: String,
	val status: String,
)
