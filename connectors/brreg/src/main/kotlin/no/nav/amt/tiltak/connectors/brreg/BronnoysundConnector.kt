package no.nav.amt.tiltak.connectors.brreg

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.connectors.brreg.dto.OverordnetEnhetDTO
import no.nav.amt.tiltak.connectors.brreg.dto.VirksomhetDTO
import no.nav.amt.tiltak.connectors.brreg.dto.href
import no.nav.amt.tiltak.core.domain.enhetsregister.Virksomhet
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class BronnoysundConnector : EnhetsregisterConnector {

	private val basePath = "https://data.brreg.no/enhetsregisteret/api"
	private val logger = LoggerFactory.getLogger(this.javaClass)

	private val restClient = OkHttpClient()
	private val objectMapper = ObjectMapper()

	override fun virksomhetsinformasjon(virksomhetsnummer: String): Virksomhet {
		val virksomhetsUrl = "$basePath/underenheter/$virksomhetsnummer"

		val virksomhet = getVirksomhet(virksomhetsUrl)
		val overordnetEnhet = getOverordnetEnhet(virksomhet.links.href("overordnetEnhet"))

		return toModel(virksomhet, overordnetEnhet)
	}

	private fun getVirksomhet(url: String): VirksomhetDTO {
		val request = Request.Builder()
			.url(url)
			.build()

		return get(request, VirksomhetDTO::class.java)
	}

	private fun getOverordnetEnhet(url: String): OverordnetEnhetDTO {
		val request = Request.Builder()
			.url(url)
			.build()

		return get(request, OverordnetEnhetDTO::class.java)
	}

	private fun <T> get(request: Request, clazz: Class<T>): T {
		val start = Instant.now()

		val response = restClient.newCall(request).execute()

		if (!response.isSuccessful || response.body == null) {
			throw IllegalArgumentException("GET kall til ${request.url} returnerer ikke body, eller er ikke successful (response code ${response.code}).")
		}

		val responseBody = objectMapper.readValue(response.body!!.byteStream(), clazz)
		logTimeOk(start, request.url.toString())

		return responseBody
	}

	private fun toModel(virksomhet: VirksomhetDTO, overordnetEnhet: OverordnetEnhetDTO): Virksomhet {
		return Virksomhet(
			organisasjonsnummer = overordnetEnhet.organisasjonsnummer,
			organisasjonsnavn = overordnetEnhet.navn,
			virksomhetsnummer = virksomhet.organisasjonsnummer,
			virksomhetsnavn = virksomhet.navn,
		)
	}

	private fun logTimeOk(start: Instant, url: String) {
		val time = Duration.between(start, Instant.now()).toMillis()
		logger.info("GET [execution time: $time ms] Url: $url")
	}
}
