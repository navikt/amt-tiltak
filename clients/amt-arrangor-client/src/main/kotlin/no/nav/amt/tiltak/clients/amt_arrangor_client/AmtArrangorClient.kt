package no.nav.amt.tiltak.clients.amt_arrangor_client

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Supplier

class AmtArrangorClient(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient()
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun hentArrangor(id: UUID): Result<ArrangorDto> {
		val request = Request.Builder()
			.url("$baseUrl/api/$id")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		return httpClient.newCall(request).execute()
			.also {
				if (!it.isSuccessful) {
					return when (it.code) {
						404 -> Result.failure(NoSuchElementException("Fant ikke arrangør med id $id"))
						else -> Result.failure(RuntimeException("Feilet ved kall mot Arrangør tjenesten. code=${it.code}"))
					}
				}
			}
			.let { it.body?.string() ?: return Result.failure(IllegalStateException("Forventet body")) }
			.let { JsonUtils.fromJsonString<ArrangorDto>(it) }
			.also { logger.info("Hentet arrangør med id $id") }
			.let { Result.success(it) }

	}

	fun hentArrangor(organisasjonsnummer: String): Result<ArrangorDto> {
		val request = Request.Builder()
			.url("$baseUrl/api/organisasjonsnummer/$organisasjonsnummer")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()

		return httpClient.newCall(request).execute()
			.also {
				if (!it.isSuccessful) {
					return when (it.code) {
						404 -> Result.failure(NoSuchElementException("Fant ikke arrangør med orgNr $organisasjonsnummer"))
						else -> Result.failure(RuntimeException("Feilet ved kall mot Arrangør tjenesten. code=${it.code}"))
					}
				}
			}
			.let { it.body?.string() ?: return Result.failure(IllegalStateException("Forventet body")) }
			.let { JsonUtils.fromJsonString<ArrangorDto>(it) }
			.also { logger.info("Hentet arrangør med orgNr $organisasjonsnummer") }
			.let { Result.success(it) }
	}

	data class ArrangorDto(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String,
		val overordnetArrangorId: UUID?,
		val deltakerlister: Set<UUID>
	)
}
