package no.nav.amt.tiltak.connectors.amt_enhetsregister

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import no.nav.amt.tiltak.core.domain.enhetsregister.Virksomhet
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class AmtEnhetsregisterConnector(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
) : EnhetsregisterConnector {

	override fun hentVirksomhet(organisasjonsnummer: String): Virksomhet {
		val request = Request.Builder()
			.url("$url/api/enhet/$organisasjonsnummer")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == 404) {
				return Virksomhet(
					navn = "Ukjent virksomhet",
					organisasjonsnummer = organisasjonsnummer,
					overordnetEnhetOrganisasjonsnummer = "999999999",
					overordnetEnhetNavn = "Ukjent virksomhet",
				)
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enhet fra amt-enhetsregister. organisasjonsnummer=${organisasjonsnummer} status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val enhetDto = fromJson(body, EnhetDto::class.java)

			return Virksomhet(
				navn = enhetDto.navn,
				organisasjonsnummer = enhetDto.organisasjonsnummer,
				overordnetEnhetOrganisasjonsnummer = enhetDto.overordnetEnhetOrganisasjonsnummer,
				overordnetEnhetNavn = enhetDto.overordnetEnhetNavn,
			)
		}
	}

}
