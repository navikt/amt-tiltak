package no.nav.amt.tiltak.ingestors.arena.domain

import java.time.LocalDateTime

internal data class ArenaData(
	val id: Int = -1,
	val tableName: String,
	val operationType: OperationType,
	val operationPosition: Long,
	val operationTimestamp: LocalDateTime,
	val ingestStatus: IngestStatus = IngestStatus.NEW,
	val ingestedTimestamp: LocalDateTime? = null,
	val ingestAttempts: Int = 0,
	val lastRetry: LocalDateTime? = null,
	val before: String? = null,
	val after: String? = null
) {
	fun markAsIgnored() = this.copy(ingestStatus = IngestStatus.IGNORED)

	fun markAsIngested() = this.copy(
		ingestStatus = IngestStatus.INGESTED,
		ingestedTimestamp = LocalDateTime.now()
	)

	fun markAsFailed() = this.copy(ingestStatus = IngestStatus.FAILED)

	fun retry() = this.copy(
		ingestStatus = IngestStatus.RETRY,
		ingestAttempts = ingestAttempts + 1,
		lastRetry = LocalDateTime.now()
	)
}
