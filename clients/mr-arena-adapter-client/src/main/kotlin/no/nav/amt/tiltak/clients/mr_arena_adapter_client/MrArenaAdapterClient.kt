package no.nav.amt.tiltak.clients.mr_arena_adapter_client

import java.util.*

interface MrArenaAdapterClient {

	fun hentGjennomforingArenaData(id: UUID): GjennomforingArenaData

}

data class GjennomforingArenaData(
	val opprettetAar: Int,
	val lopenr: Int,
	val virksomhetsnummer: String,
	val ansvarligNavEnhetId: String,
	val status: String,
)
