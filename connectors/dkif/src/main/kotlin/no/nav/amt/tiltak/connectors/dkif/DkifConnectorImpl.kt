package no.nav.amt.tiltak.connectors.dkif

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.*
import java.util.function.Supplier

class DkifConnectorImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val consumerId: String = "amt-tiltak",
) : DkifConnector {

	override fun hentBrukerKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val request: Request = Request.Builder()
			.url("$url/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.get())
			.header("Nav-Personidenter", fnr)
			.header("Nav-Consumer-Id", consumerId)
			.header("Nav-Call-Id", UUID.randomUUID().toString()) // Hvis vi skal bruke call-id så kan den propageres inn hit
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente kontaktinformasjon fra DKIF. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseDto = fromJson(body, KontaktinformasjonDto::class.java)

			val brukerKontaktinfo = responseDto.kontaktinfo.getOrDefault(fnr, null)

			return Kontaktinformasjon(
				epost = brukerKontaktinfo?.epostadresse,
				telefonnummer = brukerKontaktinfo?.mobiltelefonnummer,
			)
		}
	}

	private data class KontaktinformasjonDto(
		val kontaktinfo: Map<String, BrukerKontaktinfo>
	) {
		data class BrukerKontaktinfo(
			val personident: String,
			val epostadresse: String?,
			val mobiltelefonnummer: String?,
		)
	}

}
