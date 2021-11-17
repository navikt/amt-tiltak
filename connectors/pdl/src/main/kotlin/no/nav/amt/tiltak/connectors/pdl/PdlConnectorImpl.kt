package no.nav.amt.tiltak.connectors.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.function.Supplier

class PdlConnectorImpl(
	private val tokenProvider: Supplier<String>,
	private val pdlUrl: String,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule(),
) : PdlConnector {

	private val mediaTypeJson = "application/json".toMediaType()

	override fun hentBruker(brukerFnr: String): PdlBruker {
		val requestBody = objectMapper.writeValueAsString(
			Graphql.GraphqlQuery(
				PdlQueries.HentBruker.query,
				PdlQueries.HentBruker.Variables(brukerFnr)
			)
		)

		val request = Request.Builder()
			.url("$pdlUrl/graphql")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.addHeader("Tema", "GEN")
			.post(requestBody.toRequestBody(mediaTypeJson))
			.build()

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

}
