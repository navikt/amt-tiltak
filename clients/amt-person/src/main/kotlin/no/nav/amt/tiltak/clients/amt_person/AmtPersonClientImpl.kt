package no.nav.amt.tiltak.clients.amt_person

import no.nav.amt.tiltak.clients.amt_person.dto.NavAnsattDto
import no.nav.amt.tiltak.clients.amt_person.dto.NavBrukerDto
import no.nav.amt.tiltak.clients.amt_person.dto.NavEnhetDto
import no.nav.amt.tiltak.clients.amt_person.dto.OpprettNavBrukerDto
import no.nav.amt.tiltak.clients.amt_person.model.NavBruker
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.common.rest.client.RestClient.baseClient
import no.nav.common.utils.EnvironmentUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class AmtPersonClientImpl(
    private val baseUrl: String,
    private val tokenProvider: Supplier<String>,
    private val httpClient: OkHttpClient = baseClient(),
) : AmtPersonClient {

	private val mediaTypeJson = "application/json".toMediaType()
	private val log = LoggerFactory.getLogger(javaClass)
	override fun hentNavBruker(personIdent: String): Result<NavBruker> {
		return hentEllerOpprett("nav-bruker", NavBrukerRequest(personIdent)) { body ->
			val navBruker = fromJsonString<NavBrukerDto>(body)

			NavBruker(
				id = navBruker.id,
				personIdent = navBruker.personIdent,
				personIdentType = navBruker.personIdentType?.let { NavBruker.IdentType.valueOf(it) },
				fornavn = navBruker.fornavn,
				mellomnavn = navBruker.mellomnavn,
				etternavn = navBruker.etternavn,
				navVeilederId = navBruker.navVeilederId,
				navEnhet = navBruker.navEnhet?.let { NavBruker.NavEnhet(it.id, it.enhetId, it.navn) },
				telefon = navBruker.telefon,
				epost = navBruker.epost,
				erSkjermet = navBruker.erSkjermet,
			)
		}
	}

	override fun hentNavAnsatt(navIdent: String): Result<NavAnsatt> {
		return hentEllerOpprett("nav-ansatt", NavAnsattRequest(navIdent)) { body ->
			val navAnsattDto = fromJsonString<NavAnsattDto>(body)

			NavAnsatt(
				id = navAnsattDto.id,
				navIdent = navAnsattDto.navIdent,
				navn = navAnsattDto.navn,
				epost = navAnsattDto.epost,
				telefonnummer = navAnsattDto.telefon,
			)
		}
	}

	override fun hentNavEnhet(enhetId: String): Result<NavEnhet> {
		return hentEllerOpprett("nav-enhet", NavEnhetRequest(enhetId)) { body ->
			val navEnhetDto = fromJsonString<NavBrukerDto.NavEnhetDto>(body)

			NavEnhet(
				id = navEnhetDto.id,
				enhetId = navEnhetDto.enhetId,
				navn = navEnhetDto.navn
			)
		}
	}

	override fun migrerNavBruker(navBrukerDto: OpprettNavBrukerDto) {
		// toggler av til NavAnsatt og NavEnhet er migrert
		if (EnvironmentUtils.isDevelopment().orElse(false) || EnvironmentUtils.isProduction().orElse(false)) return

		val response = httpClient.newCall(buildRequest("migrer/nav-bruker", navBrukerDto)).execute()

		if (!response.isSuccessful) {
			log.error("Klarte ikke å opprette nav bruker med id: ${navBrukerDto.id}. Status=${response.code}")
		}
	}

	override fun migrerNavAnsatt(navAnsatt: NavAnsatt) {
		val navAnsattDto = NavAnsattDto(
			id = navAnsatt.id,
			navIdent = navAnsatt.navIdent,
			navn = navAnsatt.navn,
			telefon = navAnsatt.telefonnummer,
			epost = navAnsatt.epost,
		)

		val response = httpClient.newCall(buildRequest("migrer/nav-ansatt", navAnsattDto)).execute()

		if (!response.isSuccessful) {
			log.error("Klarte ikke å opprette nav ansatt med id: ${navAnsattDto.id}. Status=${response.code}")
		}
	}

	override fun migrerNavEnhet(navEnhet: NavEnhet) {
		val navEnhetDto = NavEnhetDto(id = navEnhet.id, enhetId = navEnhet.enhetId, navn = navEnhet.navn)
		val response = httpClient.newCall(buildRequest("migrer/nav-enhet", navEnhetDto)).execute()

		if (!response.isSuccessful) {
			log.error("Klarte ikke å opprette nav ansatt med id: ${navEnhetDto.id}. Status=${response.code}")
		}
	}

	private fun <T> hentEllerOpprett(endepunkt: String, requestBody: Any, fn: (body: String) -> T): Result<T> {
		val request = buildRequest(endepunkt, requestBody)

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				return Result.failure(RuntimeException("Klarte ikke å hente $endepunkt fra amt-person-service. status=${response.code}"))
			}
			val body = response.body?.string() ?: return Result.failure(RuntimeException("Body is missing"))
			return Result.success(fn(body))
		}
	}

	private fun buildRequest(endepunkt: String, requestBody: Any) = Request.Builder()
		.url("$baseUrl/api/$endepunkt")
		.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
		.post(toJsonString(requestBody).toRequestBody(mediaTypeJson))
		.build()

}

