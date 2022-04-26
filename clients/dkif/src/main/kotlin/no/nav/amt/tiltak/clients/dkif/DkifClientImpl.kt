package no.nav.amt.tiltak.clients.dkif

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.function.Supplier

class DkifClientImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) : DkifClient {

	override fun hentBrukerKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val request: Request = Request.Builder()
			.url("$url/rest/v1/person?inkluderSikkerDigitalPost=false")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.get())
			.header("Nav-Personident", fnr)
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente kontaktinformasjon fra DKIF. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseDto = fromJsonString<KontaktinformasjonDto>(body)

			return Kontaktinformasjon(
				epost = responseDto.epostadresse,
				telefonnummer = responseDto.mobiltelefonnummer,
			)
		}
	}

	private data class KontaktinformasjonDto(
		val epostadresse: String?,
		val mobiltelefonnummer: String?,
	)

}
