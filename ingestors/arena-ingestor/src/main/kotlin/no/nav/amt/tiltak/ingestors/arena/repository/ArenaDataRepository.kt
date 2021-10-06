package no.nav.amt.tiltak.ingestors.arena.repository

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.IngestStatus
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ArenaDataRepository(
	private val jdbcTemplate: JdbcTemplate
) {

	private companion object Table {
		const val TABLE_NAME = "arena_data"

		const val FIELD_ID = "id"
		const val FIELD_TABLE_NAME = "table_name"
		const val FIELD_OPERATION_TYPE = "operation_type"
		const val FIELD_OPERATION_POS = "operation_pos"
		const val FIELD_OPERATION_TIMESTAMP = "operation_timestamp"
		const val FIELD_INGEST_STATUS = "ingest_status"
		const val FIELD_INGESTED_TIMESTAMP = "ingested_timestamp"
		const val FIELD_INGEST_ATTEMPTS = "ingest_attempts"
		const val FIELD_LAST_RETRY = "last_retry"
		const val FIELD_BEFORE = "before"
		const val FIELD_AFTER = "after"
	}

	private val rowMapper =
		RowMapper { rs, _ ->
			ArenaData(
				id = rs.getInt(FIELD_ID),
				tableName = rs.getString(FIELD_TABLE_NAME),
				operationType = OperationType.DELETE,
				operationPosition = rs.getLong(FIELD_OPERATION_POS),
				operationTimestamp = LocalDateTime.now(),
				ingestStatus = IngestStatus.INGESTED,
				ingestedTimestamp = LocalDateTime.now(),
				ingestAttempts = rs.getInt(FIELD_INGEST_ATTEMPTS),
				lastRetry = LocalDateTime.now(),
				before = rs.getString(FIELD_BEFORE),
				after = rs.getString(FIELD_AFTER)
			)
		}

	fun insert(arenaData: ArenaData) {
		val sql =
			"""
				INSERT INT $TABLE_NAME (
					$FIELD_TABLE_NAME, $FIELD_OPERATION_TYPE, $FIELD_OPERATION_POS,
					$FIELD_OPERATION_TIMESTAMP, $FIELD_INGEST_STATUS, $FIELD_BEFORE, $FIELD_AFTER
				)
				VALUES (?, ?::arena_operation_type, ?, ?, ?::arena_ingest_status, ?, ?)
			""".trimIndent()

		jdbcTemplate.update(
			sql,
			arenaData.tableName, arenaData.operationType.toString(),
			arenaData.operationPosition, arenaData.operationTimestamp,
			arenaData.ingestStatus.toString(), arenaData.before, arenaData.after
		)
    }

    fun getUningestedData(offset: Int = 0, limit: Int = 100): List<ArenaData> {
		val sql =
			"""
				SELECT * FROM $TABLE_NAME WHERE $FIELD_INGEST_STATUS = ? OR $FIELD_INGEST_STATUS = ? ORDER BY $FIELD_OPERATION_POS ASC OFFSET ? LIMIT ?
			""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, arrayOf(IngestStatus.NEW, IngestStatus.RETRY, offset, limit))
    }

    fun getFailedData(offset: Int = 0, limit: Int = 100): List<ArenaData> {
		val sql =
			"""
				SELECT * FROM $TABLE_NAME WHERE $FIELD_INGEST_STATUS = ? ORDER BY $FIELD_OPERATION_POS ASC OFFSET ? LIMIT ?
			""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, arrayOf(IngestStatus.FAILED, offset, limit))
    }

	fun markAsIngested(id: Int) {
		val sql =
			"""
				UPDATE $TABLE_NAME SET $FIELD_INGEST_STATUS = ?::arena_ingest_status, $FIELD_INGESTED_TIMESTAMP = CURRENT_TIMESTAMP WHERE $FIELD_ID = ?
			""".trimIndent()

		jdbcTemplate.update(sql, IngestStatus.INGESTED.toString(), id)
	}

	fun incrementRetry(id: Int, currentRetries: Int) {
		val sql =
			"""
				UPDATE $TABLE_NAME SET $FIELD_INGEST_STATUS = ?::arena_ingest_status, $FIELD_INGEST_ATTEMPTS = ?, $FIELD_LAST_RETRY = CURRENT_TIMESTAMP WHERE $FIELD_ID = ?
			""".trimIndent()

		jdbcTemplate.update(sql, IngestStatus.RETRY.toString(), currentRetries + 1, id)
	}

    fun markAsFailed(id: Int) {
		val sql =
			"""
				UPDATE $TABLE_NAME SET $FIELD_INGEST_STATUS = ?::arena_ingest_status WHERE $FIELD_ID = ?
			""".trimIndent()

		jdbcTemplate.update(sql, IngestStatus.FAILED.toString(), id)
    }

}
