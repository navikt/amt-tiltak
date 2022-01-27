package no.nav.amt.tiltak.clients.veilarbarena

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class VeilarbarenaClientImpl(
	private val url: String,
	private val proxyTokenProvider: Supplier<String>,
	private val veilarbarenaTokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
	private val consumerId: String = "amt-tiltak",
) : VeilarbarenaClient {

	private val secureLog = LoggerFactory.getLogger("SecureLog")

	override fun hentBrukerOppfolgingsenhetId(fnr: String): String? {
		val request = Request.Builder()
			.url("$url/veilarbarena/api/arena/status?fnr=$fnr")
			.addHeader("Downstream-Authorization", "Bearer ${veilarbarenaTokenProvider.get()}")
			.addHeader("Authorization", "Bearer ${proxyTokenProvider.get()}")
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
