package no.nav.amt.tiltak.clients.veilarboppfolging
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class VeilarboppfolgingClientImpl(
	private val apiUrl: String,
	private val proxyTokenProvider: Supplier<String>,
	private val veilarboppfolgingTokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) : VeilarboppfolgingClient {

	override fun hentVeilederIdent(fnr: String) : String? {
		val request = Request.Builder()
			.url("$apiUrl/api/v2/veileder?fnr=$fnr")
			.header("Accept", "application/json; charset=utf-8")
			.header("Downstream-Authorization", "Bearer ${veilarboppfolgingTokenProvider.get()}")
			.header("Authorization", "Bearer ${proxyTokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			response.takeIf { !it.isSuccessful }
				?.let { throw RuntimeException("Uventet status ved kall mot veilarboppfolging ${it.code}") }

			response.takeIf { it.code == 204 }
				?.let { return null }

			response.takeIf { it.body == null }
				?.let { throw RuntimeException("Body mangler i respons fra veilarboppfolging") }

			val veilederRespons = fromJsonString<HentBrukersVeilederRespons>(response.body!!.string())

			return veilederRespons.veilederIdent.toString()
		}

	}
}
