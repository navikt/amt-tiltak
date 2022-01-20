package no.nav.amt.tiltak.connectors.pdl

import no.nav.amt.tiltak.common.json.JsonUtils.fromJson
import no.nav.amt.tiltak.common.json.JsonUtils.toJson
import no.nav.amt.tiltak.tools.graphql.Graphql
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.function.Supplier

class PdlConnectorImpl(
	private val tokenProvider: Supplier<String>,
	private val pdlUrl: String,
	private val httpClient: OkHttpClient = OkHttpClient(),
) : PdlConnector {

	private val mediaTypeJson = "application/json".toMediaType()

	override fun hentBruker(brukerFnr: String): PdlBruker {
		val requestBody = toJson(
			Graphql.GraphqlQuery(
				PdlQueries.HentBruker.query,
				PdlQueries.HentBruker.Variables(brukerFnr)
			)
		)

		val request = createGraphqlRequest(requestBody)

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente informasjon fra PDL. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing from PDL request")

			val gqlResponse = fromJson(body, PdlQueries.HentBruker.Response::class.java)

			if (gqlResponse.data == null) {
				throw RuntimeException("PDL respons inneholder ikke data")
			}

			return toPdlBruker(gqlResponse.data)
		}
	}

	override fun hentGjeldendePersonligIdent(ident: String): String {
		val requestBody = toJson(
			Graphql.GraphqlQuery(
				PdlQueries.HentGjeldendeIdent.query,
				PdlQueries.HentGjeldendeIdent.Variables(ident)
			)
		)

		val request = createGraphqlRequest(requestBody)

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente informasjon fra PDL. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing from PDL request")

			val gqlResponse = fromJson(body, PdlQueries.HentGjeldendeIdent.Response::class.java)

			if (gqlResponse.data == null) {
				throw RuntimeException("PDL respons inneholder ikke data")
			}

			return hentGjeldendeIdent(gqlResponse.data)
		}
	}

	private fun createGraphqlRequest(jsonPayload: String): Request {
		return Request.Builder()
			.url("$pdlUrl/graphql")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.addHeader("Tema", "GEN")
			.post(jsonPayload.toRequestBody(mediaTypeJson))
			.build()
	}

	private fun toPdlBruker(response: PdlQueries.HentBruker.ResponseData): PdlBruker {
		val navn = response.hentPerson.navn.firstOrNull() ?: throw RuntimeException("PDL bruker mangler navn")
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

	private fun hentGjeldendeIdent(response: PdlQueries.HentGjeldendeIdent.ResponseData): String {
		return response.hentIdenter.identer.firstOrNull()?.ident
			?: throw RuntimeException("Bruker har ikke en gjeldende ident")
	}

}
