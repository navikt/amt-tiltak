package no.nav.amt.tiltak.connectors.nom.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.NomConnector
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class NomClient(
	private val nomApiUrl: String,
	private val tokenSupplier : Supplier<String>
) : NomConnector {

	private val objectMapper = ObjectMapper().registerKotlinModule()
	companion object {
		private val logger = LoggerFactory.getLogger(NomClient::class.java)
	}

	private val client = OkHttpClient.Builder()
		.connectTimeout(10, TimeUnit.SECONDS)
		.readTimeout(15, TimeUnit.SECONDS)
		.build()

	override fun hentVeileder(ident: String) : Veileder? {
		return hentVeilederTilIdenter(listOf(ident))
			.firstOrNull()
			.also { if(it == null) logger.info("Fant ikke veileder i NOM med ident $ident") }
	}

	private fun hentVeilederTilIdenter(navIdenter: List<String>): List<Veileder> {

		val request: Request = Request.Builder()
			.url(nomApiUrl)
			.header("Accept", "application/json; charset=utf-8")
			.header("Authorization", tokenSupplier.get())
			.post(HentIdenterRequest(navIdenter).asJson().toRequestBody("application/graphql".toMediaType()))
			.build()

		client.newCall(request).execute().use { response ->
			response.takeUnless { it.isSuccessful }
				?.let { throw RuntimeException("Uventet status ved kall mot NOM ${it.code}") }

			response.takeUnless { response.body != null }
				?.let { throw IllegalStateException("Ingen body i response") }

			val nomResponse = objectMapper.readValue(response.body!!.string(), HentIdenterResponse::class.java)

			return nomResponse.toVeiledere()
		}
	}
}
