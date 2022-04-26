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

	override fun hentNavKontorNavn(enhetId: String): String {
		val request = Request.Builder()
			.url("$url/api/v1/enhet/$enhetId")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enhet enhetId=$enhetId fra norg status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseDto = fromJsonString<NavKontorDto>(body)

			return responseDto.navn
		}
	}

	override fun hentAlleNavKontorer(): List<NorgNavKontor> {
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

			val responseBody = fromJsonString<List<NavKontorDto>>(body)

			return responseBody.map { NorgNavKontor(enhetId = it.enhetNr, navn = it.navn) }
		}
	}

	private data class NavKontorDto(
		val navn: String,
		val enhetNr: String
	)

}
