package no.nav.amt.tiltak.connectors.veilarboppfolging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.core.port.VeilarboppfolgingConnector
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class VeilarboppfolgingClient(
	private val apiUrl: String,
	private val tokenSupplier : Supplier<String>
) : VeilarboppfolgingConnector {

	private val objectMapper = ObjectMapper().registerKotlinModule()
	private val httpClient = OkHttpClient()

	override fun hentVeilederIdent(fnr: String) : String? {
		val request = Request.Builder()
			.url("$apiUrl/person/$fnr/veileder")
			.header("Accept", "application/json; charset=utf-8")
			.header("Authorization", "Bearer ${tokenSupplier.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			response.takeUnless { it.isSuccessful }
				?.let {throw RuntimeException("Uventet status ved kall mot veilarboppfolging ${it.code}")}

			val veilederRespons = objectMapper.readValue(response.body!!.string(), HentBrukersVeilederRespons::class.java)
			return veilederRespons.veilederIdent
		}

	}
}
