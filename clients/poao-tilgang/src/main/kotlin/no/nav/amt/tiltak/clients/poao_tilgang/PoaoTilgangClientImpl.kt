package no.nav.amt.tiltak.clients.poao_tilgang

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import java.util.function.Supplier

class PoaoTilgangClientImpl(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) : PoaoTilgangClient {

	private val mediaTypeJson = "application/json".toMediaType()

	override fun hentAdGrupper(navIdent: String): List<AdGruppe> {
		val request = Request.Builder()
			.url("$baseUrl/api/v1/ad-gruppe")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.post(toJsonString(HentAdGrupper.Request(navIdent)).toRequestBody(mediaTypeJson))
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente ad-grupper fra poao-tilgang. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			return fromJsonString<List<HentAdGrupper.AdGruppeDto>>(body)
				.map { AdGruppe(it.id, it.name) }
		}
	}

	object HentAdGrupper {
		data class Request(
			val navIdent: String
		)

		data class AdGruppeDto(
			val id: UUID,
			val name: String
		)
	}

}
