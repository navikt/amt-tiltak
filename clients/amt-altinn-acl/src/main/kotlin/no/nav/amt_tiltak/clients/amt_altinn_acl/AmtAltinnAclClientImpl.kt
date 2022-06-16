package no.nav.amt_tiltak.clients.amt_altinn_acl

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.function.Supplier

class AmtAltinnAclClientImpl(
    private val baseUrl: String,
    private val tokenProvider: Supplier<String>,
    private val httpClient: OkHttpClient = baseClient(),
) : AmtAltinnAclClient {

	private val mediaTypeJson = "application/json".toMediaType()

	override fun hentRettigheter(norskIdent: String, rettighetIder: List<String>): List<Rettighet> {
		val requestBody = HentRettigheter.Request(norskIdent, rettighetIder)

		val request = Request.Builder()
			.url("$baseUrl/api/v1/rettighet/hent")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.post(toJsonString(requestBody).toRequestBody(mediaTypeJson))
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente rettigheter fra amt-altinn-acl. status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseBody = fromJsonString<HentRettigheter.Response>(body)

			return responseBody.rettigheter
				.map { Rettighet(it.id, it.organisasjonsnummer) }
		}
	}

	object HentRettigheter {
		data class Request(
			val norskIdent: String,
			val rettighetIder: List<String>,
		)

		data class Response(
			val rettigheter: List<Rettighet>
		) {
			data class Rettighet(
				val id: String,
				val organisasjonsnummer: String,
			)
		}

	}

}
