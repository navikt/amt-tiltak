package no.nav.amt.tiltak.connectors.veilarboppfolging
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.core.port.VeilarboppfolgingConnector
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class VeilarboppfolgingClient(
	private val apiUrl: String,
	private val tokenSupplier: Supplier<String>,
	private val consumerId: String = "amt-tiltak",
) : VeilarboppfolgingConnector {

	private val objectMapper = ObjectMapper().registerKotlinModule()
	private val httpClient = OkHttpClient()

	override fun hentVeilederIdent(fnr: String) : String? {
		val request = Request.Builder()
			.url("$apiUrl/api/v2/veileder?fnr=$fnr")
			.header("Accept", "application/json; charset=utf-8")
			.header("Authorization", "Bearer ${tokenSupplier.get()}")
			.header("Nav-Consumer-Id", consumerId)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			response.takeIf { !it.isSuccessful }
				?.let { throw RuntimeException("Uventet status ved kall mot veilarboppfolging ${it.code}") }

			response.takeIf { it.code == 204 }
				?.let { return null }

			response.takeIf { it.body == null }
				?.let { throw RuntimeException("Body mangler i respons fra veilarboppfolging") }

			val veilederRespons = objectMapper.readValue<HentBrukersVeilederRespons>(response.body!!.string())
			return veilederRespons.veilederIdent.toString()
		}

	}
}
