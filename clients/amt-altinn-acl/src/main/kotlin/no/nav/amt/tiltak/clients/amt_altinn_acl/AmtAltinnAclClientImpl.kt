package no.nav.amt.tiltak.clients.amt_altinn_acl

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.function.Supplier

class AmtAltinnAclClientImpl(
    private val baseUrl: String,
    private val tokenProvider: Supplier<String>,
    private val httpClient: OkHttpClient = baseClient(),
) : AmtAltinnAclClient {

	override fun hentTiltaksarrangorRoller(norskIdent: String): List<TiltaksarrangorAnsattRoller> {
		val request = Request.Builder()
			.url("$baseUrl/api/v1/rolle/tiltaksarrangor?norskIdent=$norskIdent")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente tiltaksarrangør roller fra amt-altinn-acl. status=${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val responseBody = fromJsonString<HentTiltaksarrangorRoller.Response>(body)

			return responseBody.roller.map {
				TiltaksarrangorAnsattRoller(it.organisasjonsnummer, it.roller.map(::mapTiltaksarrangorRolle))
			}
		}
	}

	private fun mapTiltaksarrangorRolle(rolle: String): TiltaksarrangorAnsattRolle {
		return when (rolle) {
			"KOORDINATOR" -> TiltaksarrangorAnsattRolle.KOORDINATOR
			"VEILEDER" -> TiltaksarrangorAnsattRolle.VEILEDER
			else -> throw IllegalArgumentException("Ukjent tiltaksarrangør rolle $rolle")
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

	object HentTiltaksarrangorRoller {
		data class Response(
			val roller: List<TiltaksarrangorRoller>
		) {
			data class TiltaksarrangorRoller(
				val organisasjonsnummer: String,
				val roller: List<String>,
			)
		}
	}

}
