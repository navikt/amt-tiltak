package no.nav.amt.tiltak.connectors.veilarbarena

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class VeilarbarenaConnectorImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val consumerId: String = "amt-tiltak",
	private val objectMapper: ObjectMapper = ObjectMapper()
		.registerKotlinModule()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
) : VeilarbarenaConnector {

	private val log = LoggerFactory.getLogger(this::class.java)

	override fun hentBrukerOppfolgingsenhetId(fnr: String): String? {
		val request = Request.Builder()
			.url("$url/veilarbarena/api/arena/status?fnr=$fnr")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.addHeader("Nav-Consumer-Id", consumerId)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == 404) {
				log.warn("Bruker finnes ikke i veilarbarena")
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente status fra veilarbarena. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val statusDto = objectMapper.readValue(body, BrukerArenaStatusDto::class.java)

			return statusDto.oppfolgingsenhet
		}
	}

	private data class BrukerArenaStatusDto(
		var oppfolgingsenhet: String?
	)

}
