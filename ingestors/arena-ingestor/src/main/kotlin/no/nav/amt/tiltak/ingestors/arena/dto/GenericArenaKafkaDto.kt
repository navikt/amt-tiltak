package no.nav.amt.tiltak.ingestors.arena.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import no.nav.amt.tiltak.ingestors.arena.repository.CreateArenaData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class ArenaOpType(val type: String) {
    I("I"),    // insert
    U("U"),    // update
    D("D"),    // delete
}

data class StringArenaKafkaDto(
    val table: String, // Ex: ARENA_GOLDENGATE.TILTAKSAKTIVITET
    val op_type: ArenaOpType,
    val op_ts: String, // Ex: 2021-09-08 08:06:21.371796 (Er ikke ISO 8601)
    val current_ts: String, // Ex: 2021-09-08T08:07:50.726030 (Er ISO 8601)
    val pos: String, // Ex: 00000000000037764187
    val after: JsonNode?,
    val before: JsonNode?
) {

    fun toArenaData(): CreateArenaData {
        val mapper = jacksonObjectMapper()
        val opTsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val beforeString = mapper.writeValueAsString(before)
        val afterString = mapper.writeValueAsString(after)

        return CreateArenaData(
            tableName = table,
            operationType = OperationType.fromArena(op_type),
            operationPosition = pos.toLong(),
            operationTimestamp = LocalDateTime.parse(op_ts, opTsFormatter),
            before = if (beforeString != null && beforeString != "null") beforeString else null,
            after = if (afterString != null && afterString != "null") afterString else null,
        )
    }
}
