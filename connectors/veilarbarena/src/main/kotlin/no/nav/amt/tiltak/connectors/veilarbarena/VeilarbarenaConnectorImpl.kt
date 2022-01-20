package no.nav.amt.tiltak.connectors.veilarbarena

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class VeilarbarenaConnectorImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val consumerId: String = "amt-tiltak",
) : VeilarbarenaConnector {

	private val secureLog = LoggerFactory.getLogger("SecureLog")

	override fun hentBrukerOppfolgingsenhetId(fnr: String): String? {
		val request = Request.Builder()
			.url("$url/veilarbarena/api/arena/status?fnr=$fnr")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.addHeader("Nav-Consumer-Id", consumerId)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == 404) {
				secureLog.warn("Fant ikke bruker med fnr=$fnr i veilarbarena")
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente status fra veilarbarena. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val statusDto = fromJson(body, BrukerArenaStatusDto::class.java)

			return statusDto.oppfolgingsenhet
		}
	}

	private data class BrukerArenaStatusDto(
		var oppfolgingsenhet: String?
	)

}
