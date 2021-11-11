package no.nav.amt.tiltak.ingestors.arena.domain

import java.time.LocalDateTime
import java.util.*

data class ArenaData(
    val id: Int,
    val tableName: String,
    val operationType: OperationType,
    val operationPosition: Long,
    val operationTimestamp: LocalDateTime,
    val ingestStatus: IngestStatus,
    val ingestedTimestamp: LocalDateTime?,
    val ingestAttempts: Int,
    val lastRetry: LocalDateTime?,
    val before: String?,
    val after: String?
)
