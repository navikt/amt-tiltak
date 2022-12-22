package no.nav.amt.tiltak.clients.mr_arena_adapter_client

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import java.util.function.Supplier

class MrArenaAdapterClientImpl(
    private val baseUrl: String,
    private val tokenProvider: Supplier<String>,
    private val httpClient: OkHttpClient = baseClient(),
) : MrArenaAdapterClient {

	override fun hentGjennomforingArenaData(id: UUID): GjennomforingArenaData {
		val request = Request.Builder()
			.url("$baseUrl/TODO/$id")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente gjennomføring arenadata fra Mulighetsrommet. status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseBody = fromJsonString<HentGjennomforingArenaData.Response>(body)

			return GjennomforingArenaData(
				opprettetAar = responseBody.opprettetAar,
				lopenr = responseBody.lopenr,
				virksomhetsnummer = responseBody.virksomhetsnummer,
				ansvarligNavEnhetId = responseBody.ansvarligNavEnhetId,
				status = responseBody.status,
			)
		}
	}

	object HentGjennomforingArenaData {
		data class Response(
			val opprettetAar: Int,
			val lopenr: Int,
			val virksomhetsnummer: String,
			val ansvarligNavEnhetId: String,
			val status: String,
		)
	}


}
