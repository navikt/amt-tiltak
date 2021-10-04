package no.nav.amt.tiltak.ingestors.arena.domain

import java.time.LocalDateTime
import java.util.*

data class ArenaData(
    val id: UUID,
    val table: String,
    val operationType: OperationType,
    val operationTimestamp: LocalDateTime,
    val ingestStatus: IngestStatus,
    val ingestedTimestamp: LocalDateTime?,
    val before: String?,
    val after: String
)
