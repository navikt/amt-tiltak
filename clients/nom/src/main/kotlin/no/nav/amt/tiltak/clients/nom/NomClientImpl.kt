package no.nav.amt.tiltak.clients.nom

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.tools.graphql.Graphql
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class NomClientImpl(
	private val url: String,
	private val tokenSupplier : Supplier<String>,
	private val httpClient: OkHttpClient = baseClient(),
) : NomClient {

	companion object {
		private val mediaTypeJson = "application/json".toMediaType()

		private val log = LoggerFactory.getLogger(NomClientImpl::class.java)
	}

	override fun hentNavAnsatt(navIdent: String): NomNavAnsatt? {
		return hentVeilederTilIdenter(listOf(navIdent))
			.firstOrNull()
			.also { if(it == null) log.info("Fant ikke veileder i NOM med ident $navIdent") }
	}

	private fun hentVeilederTilIdenter(navIdenter: List<String>): List<NomNavAnsatt> {
		val requestBody = toJsonString(
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

			val hentIdenterResponse = fromJsonString<NomQueries.HentIdenter.Response>(response.body!!.string())

			return toVeiledere(hentIdenterResponse)
		}
	}

	private fun toVeiledere(hentIdenterResponse: NomQueries.HentIdenter.Response): List<NomNavAnsatt> {
		return hentIdenterResponse.data?.ressurser?.mapNotNull {
			if (it.code != NomQueries.HentIdenter.ResultCode.OK || it.ressurs == null) {
				log.warn("Fant ikke veileder i NOM. statusCode=${it.code}")
				return@mapNotNull null
			}

			val telefonnummer = hentTjenesteTelefonnummer(it.ressurs.telefon)

			val ansatt = it.ressurs

			NomNavAnsatt(
				navIdent = ansatt.navIdent,
				navn = ansatt.visningsNavn ?: "${ansatt.fornavn} ${ansatt.etternavn}",
				epost = it.ressurs.epost,
				telefonnummer = telefonnummer
			)
		} ?: emptyList()
	}

	private fun hentTjenesteTelefonnummer(telefonnumere: List<NomQueries.HentIdenter.Telefon>): String? {
		return telefonnumere.find { it.type == "NAV_KONTOR_TELEFON" }?.nummer
			?: telefonnumere.find { it.type == "NAV_TJENESTE_TELEFON" }?.nummer
	}
}
