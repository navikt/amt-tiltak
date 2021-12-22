package no.nav.amt.tiltak.ingestors.arena.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal enum class ArenaOpType(val type: String) {
	I("I"),    // insert
	U("U"),    // update
	D("D"),    // delete
}


internal data class StringArenaKafkaDto(
	val table: String, // Ex: ARENA_GOLDENGATE.TILTAKSAKTIVITET
	@JsonProperty("op_type") val operationType: ArenaOpType,
	@JsonProperty("op_ts") val operationTimestamp: String, // Ex: 2021-09-08 08:06:21.371796 (Er ikke ISO 8601)
	@JsonProperty("current_ts") val currentTimestamp: String, // Ex: 2021-09-08T08:07:50.726030 (Er ISO 8601)
	val pos: String, // Ex: 00000000000037764187
	val after: JsonNode?,
	val before: JsonNode?
) {

	fun toArenaData(): ArenaData {
		val mapper = jacksonObjectMapper()

		val beforeString = mapper.writeValueAsString(before)
		val afterString = mapper.writeValueAsString(after)

		return ArenaData(
			tableName = table,
			operationType = OperationType.fromArena(operationType),
			operationPosition = pos.toLong(),
			operationTimestamp = getOperationTimestamp(),
			before = if (beforeString != null && beforeString != "null") beforeString else null,
			after = if (afterString != null && afterString != "null") afterString else null,
		)
	}

	fun getOperationTimestamp(): LocalDateTime {
		val opTsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
		return LocalDateTime.parse(operationTimestamp, opTsFormatter)
	}
}
