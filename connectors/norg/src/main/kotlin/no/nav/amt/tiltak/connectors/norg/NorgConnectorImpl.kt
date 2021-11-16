package no.nav.amt.tiltak.connectors.norg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class NorgConnectorImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper()
		.registerKotlinModule()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
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

			val responseDto = objectMapper.readValue(body, NavEnhetDto::class.java)

			return responseDto.navn
		}
	}

	private data class NavEnhetDto(
		val navn: String
	)

}
