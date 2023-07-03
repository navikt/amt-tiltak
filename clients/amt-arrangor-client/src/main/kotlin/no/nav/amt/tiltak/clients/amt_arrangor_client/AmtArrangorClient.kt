package no.nav.amt.tiltak.clients.amt_arrangor_client

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.common.rest.client.RestClient.baseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.function.Supplier

class AmtArrangorClient(
	private val baseUrl: String,
	private val tokenProvider: Supplier<String>,
	private val httpClient: OkHttpClient = baseClient()
) {

	private val log = LoggerFactory.getLogger(javaClass)
	private val mediaTypeJson = "application/json".toMediaType()

	fun hentAnsatt(personident: String): AnsattDto? {
		val request = Request.Builder()
			.url("$baseUrl/api/service/ansatt")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.post(JsonUtils.toJsonString(AnsattRequestBody(personident)).toRequestBody(mediaTypeJson))
			.build()
		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				if (response.code == 404) {
					log.info("Ansatt finnes ikke hos amt-arrangør")
					return null
				}
				log.error("Kunne ikke hente ansatt fra amt-arrangør. Status=${response.code}")
				throw RuntimeException("Kunne ikke hente ansatt fra amt-arrangør. Status=${response.code}")
			}
			val body = response.body?.string() ?: throw RuntimeException("Body is missing")
			return JsonUtils.fromJsonString<AnsattDto>(body)
		}
	}

	fun hentAnsatt(ansattId: UUID): AnsattDto? {
		val request = Request.Builder()
			.url("$baseUrl/api/service/ansatt/$ansattId")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()
		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				if (response.code == 404) {
					log.info("Ansatt finnes ikke hos amt-arrangør")
					return null
				}
				log.error("Kunne ikke hente ansatt fra amt-arrangør. Status=${response.code}")
				throw RuntimeException("Kunne ikke hente ansatt fra amt-arrangør. Status=${response.code}")
			}
			val body = response.body?.string() ?: throw RuntimeException("Body is missing")
			return JsonUtils.fromJsonString<AnsattDto>(body)
		}
	}

	fun hentArrangor(orgnummer: String): ArrangorMedOverordnetArrangor? {
		val request = Request.Builder()
			.url("$baseUrl/api/service/arrangor/organisasjonsnummer/$orgnummer")
			.addHeader("Authorization", "Bearer ${tokenProvider.get()}")
			.get()
			.build()
		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				if (response.code == 404) {
					log.info("Arrangør med orgnummer $orgnummer finnes ikke hos amt-arrangør")
					return null
				}
				log.error("Kunne ikke hente arrangør med orgnummer $orgnummer fra amt-arrangør. Status=${response.code}")
				throw RuntimeException("Kunne ikke hente arrangør med orgnummer $orgnummer fra amt-arrangør. Status=${response.code}")
			}
			val body = response.body?.string() ?: throw RuntimeException("Body is missing")
			return JsonUtils.fromJsonString<ArrangorMedOverordnetArrangor>(body)
		}
	}

	data class AnsattDto(
		val id: UUID,
		val personalia: PersonaliaDto,
		val arrangorer: List<TilknyttetArrangorDto>
	)

	data class PersonaliaDto(
		val personident: String,
		val personId: UUID?,
		val navn: Navn
	)

	data class Navn(
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String
	)

	data class TilknyttetArrangorDto(
		val arrangorId: UUID,
		val arrangor: Arrangor,
		val overordnetArrangor: Arrangor?,
		val roller: List<AnsattRolle>,
		val veileder: List<VeilederDto>,
		val koordinator: List<UUID>
	)

	data class Arrangor(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String
	)

	data class VeilederDto(
		val deltakerId: UUID,
		val type: VeilederType
	)

	enum class AnsattRolle {
		KOORDINATOR,
		VEILEDER
	}

	enum class VeilederType {
		VEILEDER,
		MEDVEILEDER
	}

	data class AnsattRequestBody(
		val personident: String
	)

	data class ArrangorMedOverordnetArrangor(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String,
		val overordnetArrangor: Arrangor?
	)
}
