package no.nav.amt.tiltak.connectors.nom.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.NomConnector
import no.nav.amt.tiltak.tools.graphql.Graphql
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class NomClient(
	private val url: String,
	private val tokenSupplier : Supplier<String>,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
) : NomConnector {

	companion object {
		private val mediaTypeJson = "application/json".toMediaType()

		private val log = LoggerFactory.getLogger(NomClient::class.java)
	}

	override fun hentVeileder(navIdent: String): Veileder? {
		return hentVeilederTilIdenter(listOf(navIdent))
			.firstOrNull()
			.also { if(it == null) log.info("Fant ikke veileder i NOM med ident $navIdent") }
	}

	private fun hentVeilederTilIdenter(navIdenter: List<String>): List<Veileder> {
		val requestBody = objectMapper.writeValueAsString(
			Graphql.GraphqlQuery(
				NomQueries.HentIdenter.query,
				NomQueries.HentIdenter.Variables(navIdenter)
			)
		)

		val request: Request = Request.Builder()
			.url("$url/graphql")
			.header("Accept", mediaTypeJson.toString())
			.header("Authorization", "Bearer ${tokenSupplier.get()}")
			.post(requestBody.toRequestBody(mediaTypeJson))
			.build()

		httpClient.newCall(request).execute().use { response ->
			response.takeUnless { it.isSuccessful }
				?.let { throw RuntimeException("Uventet status ved kall mot NOM ${it.code}") }

			response.takeUnless { response.body != null }
				?.let { throw IllegalStateException("Ingen body i response") }

			val hentIdenterResponse = objectMapper.readValue(response.body!!.string(), NomQueries.HentIdenter.Response::class.java)

			return toVeiledere(hentIdenterResponse)
		}
	}

	private fun toVeiledere(hentIdenterResponse: NomQueries.HentIdenter.Response): List<Veileder> {
		return hentIdenterResponse.data?.ressurser?.mapNotNull {
			if (it.code != NomQueries.HentIdenter.ResultCode.OK || it.ressurs == null) {
				log.warn("Fant ikke veileder i NOM. statusCode=${it.code}")
				return@mapNotNull null
			}

			Veileder(
				navIdent = it.ressurs.navIdent,
				visningsNavn = it.ressurs.visningsNavn,
				fornavn = it.ressurs.fornavn,
				etternavn = it.ressurs.etternavn,
				epost = it.ressurs.epost,
			)
		} ?: emptyList()
	}
}
