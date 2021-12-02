package no.nav.amt.tiltak.connectors.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.tools.graphql.Graphql
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.function.Supplier

class PdlClientImpl(
	private val tokenProvider: Supplier<String>,
	private val pdlUrl: String,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule(),
) : PdlClient {

	private val mediaTypeJson = "application/json".toMediaType()

	override fun hentBruker(brukerFnr: String): PdlBruker {
		val requestBody = objectMapper.writeValueAsString(
			Graphql.GraphqlQuery(
				PdlQueries.HentBruker.query,
				PdlQueries.HentBruker.Variables(brukerFnr)
			)
		)

		val request = buildGqlRequest(requestBody.toRequestBody(mediaTypeJson))

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente informasjon fra PDL. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing from PDL request")

			val gqlResponse = objectMapper.readValue(body, PdlQueries.HentBruker.Response::class.java)

			if (gqlResponse.data == null) {
				throw RuntimeException("PDL respons inneholder ikke data")
			}

			return toPdlBruker(gqlResponse.data)
		}
	}

	override fun hentFnr(aktorId: String): String {
		val requestBody = objectMapper.writeValueAsString(
			Graphql.GraphqlQuery(
				PdlQueries.HentGjeldendeIdent.query,
				PdlQueries.HentGjeldendeIdent.Variables(aktorId)
			)
		)

		val request = buildGqlRequest(requestBody.toRequestBody(mediaTypeJson))

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente gjeldende ident fra PDL. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing from PDL request")

			val gqlResponse = objectMapper.readValue(body, PdlQueries.HentGjeldendeIdent.Response::class.java)

			if (gqlResponse.data == null) {
				throw RuntimeException("PDL respons inneholder ikke data")
			}

			return toIdent(gqlResponse.data)
		}
	}

	private fun buildGqlRequest(requestBody: RequestBody): Request {
		return Request.Builder()
			.url("$pdlUrl/graphql")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.addHeader("Tema", "GEN")
			.post(requestBody)
			.build()
	}

	private fun toPdlBruker(response: PdlQueries.HentBruker.ResponseData): PdlBruker {
		val navn = response.hentPerson.navn.firstOrNull() ?: throw IllegalStateException("PDL bruker mangler navn")
		val telefonnummer = getTelefonnummer(response.hentPerson.telefonnummer)

		return PdlBruker(
			fornavn = navn.fornavn,
			mellomnavn = navn.mellomnavn,
			etternavn = navn.etternavn,
			telefonnummer = telefonnummer
		)
	}

	private fun getTelefonnummer(telefonnummere: List<PdlQueries.HentBruker.Telefonnummer>): String? {
		val prioritertNummer = telefonnummere.minByOrNull { it.prioritet } ?: return null

		return "${prioritertNummer.landskode} ${prioritertNummer.nummer}"
	}

	private fun toIdent(response: PdlQueries.HentGjeldendeIdent.ResponseData): String {
		return response.hentIdenter.identer.firstOrNull()?.ident ?: throw IllegalStateException("Fant ikke ident til bruker i PDL")
	}

}
