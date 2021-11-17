package no.nav.amt.tiltak.connectors.dkif

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.function.Supplier

class DkifConnectorImpl(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper()
		.registerKotlinModule()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
) : DkifConnector {

	override fun hentBrukerKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val request: Request = Request.Builder()
			.url("$url/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.get())
			.header("Nav-Personidenter", fnr)
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente kontaktinformasjon fra DKIF. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseDto = objectMapper.readValue(body, KontaktinformasjonDto::class.java)

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
