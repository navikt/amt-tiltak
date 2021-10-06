package no.nav.amt.tiltak.connectors.nom.client

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.connectors.nom.graphql.NomGraphqlRequest
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class NomGraphqlClient(
	private val nomApiUrl: String,
	private val tokenSupplier : Supplier<String>
) {

	private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
	private val objectMapper = ObjectMapper();

	private val client = OkHttpClient.Builder()
		.connectTimeout(10, TimeUnit.SECONDS)
		.readTimeout(15, TimeUnit.SECONDS)
		.build()

	private fun createBearerToken(): String = tokenSupplier.get()

	private fun hentVeilederTilIdenter(navIdenter: List<String>): List<Veileder> {

		val request: Request = Request.Builder()
			.url(nomApiUrl)
			.header("Accept", MEDIA_TYPE_JSON.toString())
			.header("Authorization", createBearerToken())
			.post(RequestBody.create(MEDIA_TYPE_JSON, NomGraphqlRequest(variables = navIdenter).toJson()))
			.build()

		client.newCall(request).execute().use { response ->
			//Logge info
			response.takeUnless { it.isSuccessful }
				?.let { throw RuntimeException("Uventet status ved kall mot NOM ${it.code}") }

			response.takeUnless { response.body == null }
				?.let { throw IllegalStateException("Ingen body i response") }

			val nomResponse = objectMapper.readValue(response.body!!.string(), NomResponse::class.java);
			return nomResponse.tilVeiledere()
			/*
			GraphqlUtils.logWarningIfError(graphqlResponse)
			GraphqlUtils.throwIfMissingData(graphqlResponse)*/
		}
	}
}
