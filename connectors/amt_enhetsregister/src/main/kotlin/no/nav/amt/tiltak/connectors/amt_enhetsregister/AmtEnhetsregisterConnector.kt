package no.nav.amt.tiltak.connectors.amt_enhetsregister

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.core.domain.enhetsregister.Virksomhet
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class AmtEnhetsregisterConnector(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule(),
) : EnhetsregisterConnector {

	override fun hentVirksomhet(organisasjonsnummer: String): Virksomhet {
		val request = Request.Builder()
			.url("$url/api/enhet/$organisasjonsnummer")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enhet fra amt-enhetsregister. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val enhetDto = objectMapper.readValue(body, EnhetDto::class.java)

			return Virksomhet(
				navn = enhetDto.navn,
				organisasjonsnummer = enhetDto.organisasjonsnummer,
				overordnetEnhetOrganisasjonsnummer = enhetDto.overordnetEnhetOrganisasjonsnummer,
				overordnetEnhetNavn = enhetDto.overordnetEnhetNavn,
			)
		}
	}

}
