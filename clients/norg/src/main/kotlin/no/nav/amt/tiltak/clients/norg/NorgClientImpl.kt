package no.nav.amt.tiltak.clients.norg

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class NorgClientImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) : NorgClient {

	override fun hentNavEnhet(enhetId: String): NorgNavEnhet? {
		val request = Request.Builder()
			.url("$url/api/v1/enhet/$enhetId")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == 404) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enhet enhetId=$enhetId fra norg status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			return fromJsonString<NavEnhetDto>(body)
				.let { NorgNavEnhet(it.enhetNr, it.navn) }
		}
	}

	override fun hentAlleNavEnheter(): List<NorgNavEnhet> {
		val request = Request.Builder()
			.url("$url/api/v1/enhet/")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente alle enheter fra Norg status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseBody = fromJsonString<List<NavEnhetDto>>(body)

			return responseBody.map { NorgNavEnhet(enhetId = it.enhetNr, navn = it.navn) }
		}
	}

	private data class NavEnhetDto(
		val navn: String,
		val enhetNr: String
	)

}
