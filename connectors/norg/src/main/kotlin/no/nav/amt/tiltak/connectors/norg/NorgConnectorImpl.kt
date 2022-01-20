package no.nav.amt.tiltak.connectors.norg

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class NorgConnectorImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
) : NorgConnector {

	override fun hentNavKontorNavn(enhetId: String): String {
		val request = Request.Builder()
			.url("$url/api/v1/enhet/$enhetId")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enhet fra norg. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseDto = fromJson(body, NavEnhetDto::class.java)

			return responseDto.navn
		}
	}

	private data class NavEnhetDto(
		val navn: String
	)

}
