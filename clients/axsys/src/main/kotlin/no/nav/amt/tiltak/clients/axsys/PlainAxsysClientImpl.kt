package no.nav.amt.tiltak.clients.axsys

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.function.Supplier

internal class PlainAxsysClient(
	private val url: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) : AxsysClient {

	private val mapper = JsonUtils.getObjectMapper()

	override fun hentTilganger(brukerident: String): Enheter {

		httpClient.newCall(axysRequest(brukerident)).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente veileders tilganger fra axsys. Status: ${response.code}")
			}

			return response.body?.string().let {
				mapper.readValue(it, Enheter::class.java)
			} ?: throw RuntimeException("Body is missing")
		}
	}

	private fun axysRequest(brukerident: String) = Request.Builder()
		.url("$url/api/v2/tilgang/$brukerident?inkluderAlleEnheter=false") // TODO injection prone?
		.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
		.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.get())
		.build()
}
