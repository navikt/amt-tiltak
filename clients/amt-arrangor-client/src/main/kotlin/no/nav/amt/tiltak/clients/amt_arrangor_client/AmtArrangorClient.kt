package no.nav.amt.tiltak.clients.amt_arrangor_client

import no.nav.amt.tiltak.common.json.JsonUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class AmtArrangorClient(
	private val baseUrl: String,
	private val httpClient: OkHttpClient
) {

	fun hentArrangor(id: UUID): Result<ArrangorDto> {
		val request = Request.Builder()
			.url("$baseUrl/api/arrangor/$id")
			.get()
			.build()

		return execute<ArrangorDto>(request)
	}

	fun hentArrangor(organisasjonsnummer: String): Result<ArrangorDto> = Request.Builder()
		.url("$baseUrl/api/arrangor/organisasjonsnummer/$organisasjonsnummer")
		.get()
		.build()
		.let { execute<ArrangorDto>(it) }


	fun hentAnsatt(): Result<AnsattDto> = Request.Builder()
		.url("$baseUrl/api/ansatt")
		.get()
		.build()
		.let { execute<AnsattDto>(it) }


	fun hentAnsatt(id: UUID): Result<AnsattDto> = Request.Builder()
		.url("$baseUrl/api/ansatt/$id")
		.get()
		.build()
		.let { execute<AnsattDto>(it) }


	fun setAsKoordinator(deltakerlisteId: UUID): Result<AnsattDto> = Request.Builder()
		.url("$baseUrl/api/ansatt/koordinator/$deltakerlisteId")
		.post("".toRequestBody())
		.build()
		.let { execute<AnsattDto>(it) }

	fun removeAsKoordinator(deltakerlisteId: UUID): Result<AnsattDto> = Request.Builder()
		.url("$baseUrl/api/ansatt/koordinator/$deltakerlisteId")
		.delete()
		.build()
		.let { execute(it) }

	fun setAsVeileder(deltakerId: UUID, arrangorId: UUID, type: VeilederType): Result<AnsattDto> = Request.Builder()
		.url("$baseUrl/api/ansatt/veileder")
		.post(createSetVeilederBody(deltakerId, arrangorId, type))
		.build()
		.let { execute(it) }

	fun removeAsVeileder(deltakerId: UUID): Result<AnsattDto> = Request.Builder()
		.url("$baseUrl/api/ansatt/veileder/$deltakerId")
		.delete()
		.build()
		.let { execute(it) }

	private fun createSetVeilederBody(deltakerId: UUID, arrangorId: UUID, type: VeilederType): RequestBody =
		JsonUtils.toJsonString(SetVeilederForDeltakerRequestBody(deltakerId, arrangorId, type)).toRequestBody()

	private inline fun <reified T> execute(request: Request): Result<T> = httpClient.newCall(request).execute()
		.also {
			if (!it.isSuccessful) {
				return when (val code = it.code) {
					404 -> Result.NotFound()
					else -> throw RuntimeException("${request.url} returnerte $code")
				}
			}
		}
		.let { it.body?.string() ?: throw IllegalStateException("Forventet body") }
		.let { Result.OK(JsonUtils.fromJsonString(it)) }


	sealed class Result<T> {
		data class OK<T>(val result: T) : Result<T>()
		class NotFound<T> : Result<T>()
	}

	data class ArrangorDto(
		val id: UUID,
		val navn: String,
		val organisasjonsnummer: String,
		val overordnetArrangorId: UUID?,
		val deltakerlister: Set<UUID>
	)


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
		val roller: List<AnsattRolle>,
		val veileder: List<VeilederDto>,
		val koordinator: List<UUID>
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

	data class SetVeilederForDeltakerRequestBody(
		val deltakerId: UUID,
		val arrangorId: UUID,
		val type: VeilederType
	)

}
