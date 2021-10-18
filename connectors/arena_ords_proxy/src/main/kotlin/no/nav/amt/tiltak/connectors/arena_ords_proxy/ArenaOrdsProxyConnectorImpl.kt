package no.nav.amt.tiltak.connectors.arena_ords_proxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.amt.tiltak.core.port.Arbeidsgiver
import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatus
import java.util.function.Supplier

class ArenaOrdsProxyConnectorImpl(
	private val tokenProvider: Supplier<String>,
	private val arenaOrdsProxyUrl: String,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule(),
) : ArenaOrdsProxyConnector {

	override fun hentFnr(arenaPersonId: String): String? {
		val request = Request.Builder()
			.url("$arenaOrdsProxyUrl/api/ords/fnr?personId=$arenaPersonId")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == HttpStatus.NOT_FOUND.value()) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente fnr for Arena personId. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			return objectMapper.readValue(body, HentFnrResponse::class.java).fnr
		}
	}

	override fun hentArbeidsgiver(arenaArbeidsgiverId: String): Arbeidsgiver? {
		val request = Request.Builder()
			.url("$arenaOrdsProxyUrl/api/ords/arbeidsgiver?arbeidsgiverId=$arenaArbeidsgiverId")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == HttpStatus.NOT_FOUND.value()) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente arbeidsgiver. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val arbeidsgiverResponse = objectMapper.readValue(body, ArbeidsgiverResponse::class.java)

			return Arbeidsgiver(
				virksomhetsnummer = arbeidsgiverResponse.virksomhetsnummer,
				organisasjonsnummerMorselskap = arbeidsgiverResponse.organisasjonsnummerMorselskap
			)
		}
	}

	override fun hentVirksomhetsnummer(virksomhetsnummer: String): String {
		return hentArbeidsgiver(virksomhetsnummer)?.virksomhetsnummer
			?: throw UnsupportedOperationException("Kan ikke hente virksomhetsnummer på en arbeidsgiver som ikke eksisterer.")
	}

	private data class HentFnrResponse(
		val fnr: String,
	)

	private data class ArbeidsgiverResponse(
		val virksomhetsnummer: String,
		val organisasjonsnummerMorselskap: String,
	)

}
