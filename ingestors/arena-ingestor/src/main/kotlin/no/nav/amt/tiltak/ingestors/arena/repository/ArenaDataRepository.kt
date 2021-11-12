package no.nav.amt.tiltak.ingestors.arena.repository

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.IngestStatus
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
open class ArenaDataRepository(
	private val jdbcTemplate: JdbcTemplate,
	private val namedJdbcTemplate: NamedParameterJdbcTemplate
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	companion object Table {
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

	val rowMapper =
		RowMapper { rs, _ ->
			ArenaData(
				id = rs.getInt(FIELD_ID),
				tableName = rs.getString(FIELD_TABLE_NAME),
				operationType = OperationType.valueOf(rs.getString(FIELD_OPERATION_TYPE)),
				operationPosition = rs.getLong(FIELD_OPERATION_POS),
				operationTimestamp = rs.getTimestamp(FIELD_OPERATION_TIMESTAMP).toLocalDateTime(),
				ingestStatus = IngestStatus.valueOf(rs.getString(FIELD_INGEST_STATUS)),
				ingestedTimestamp = rs.getTimestamp(FIELD_INGESTED_TIMESTAMP)?.toLocalDateTime(),
				ingestAttempts = rs.getInt(FIELD_INGEST_ATTEMPTS),
				lastRetry = rs.getTimestamp(FIELD_LAST_RETRY)?.toLocalDateTime(),
				before = rs.getString(FIELD_BEFORE),
				after = rs.getString(FIELD_AFTER)
			)
		}

	fun insert(arenaData: ArenaData) {
		val sql =
			"""
				INSERT INTO arena_data (table_name, operation_type, operation_pos, operation_timestamp, ingest_status, before, after)
				VALUES (:tableName,
						:operationType::arena_operation_type,
						:operationPosition,
						:operationTimestamp,
						:ingestStatus::arena_ingest_status,
						:before::json,
						:after::json)
				ON CONFLICT(id) DO UPDATE SET ingest_status      = :ingestStatus::arena_ingest_status,
										      ingested_timestamp = :ingestedTimestamp,
										      ingest_attempts    = :ingestAttempts,
										      last_retry         = :lastRetry
			""".trimIndent()

		namedJdbcTemplate.update(
			sql,
			arenaData.asParameterSource()
		)
	}

	private fun ArenaData.asParameterSource() = MapSqlParameterSource().addValues(mapOf(
			"id" to id,
			"tableName" to tableName,
			"operationType" to operationType.name,
			"operationPosition" to operationPosition,
			"operationTimestamp" to operationTimestamp,
			"ingestStatus" to ingestStatus.name,
			"ingestedTimestamp" to ingestedTimestamp,
			"ingestAttempts" to ingestAttempts,
			"lastRetry" to lastRetry,
			"before" to before,
			"after" to after
	))

	fun getUningestedData(offset: Int = 0, limit: Int = 100): List<ArenaData> {
		val sql =
			"""
				SELECT * FROM $TABLE_NAME
				 WHERE $FIELD_INGEST_STATUS = ?::arena_ingest_status
				 	OR $FIELD_INGEST_STATUS = ?::arena_ingest_status
				 ORDER BY $FIELD_OPERATION_POS ASC OFFSET ? LIMIT ?
			""".trimIndent()

		return jdbcTemplate.query(
			sql,
			rowMapper,
			IngestStatus.NEW.toString(),
			IngestStatus.RETRY.toString(),
			offset,
			limit
		)
	}

	fun getUningestedData(tableName: String, offset: Int = 0, limit: Int = 100): List<ArenaData> {
		val sql =
			"""
				SELECT * FROM $TABLE_NAME
				 WHERE table_name = ?
				 	AND ($FIELD_INGEST_STATUS = ?::arena_ingest_status
				 	OR $FIELD_INGEST_STATUS = ?::arena_ingest_status)
				 ORDER BY $FIELD_OPERATION_POS ASC OFFSET ? LIMIT ?
			""".trimIndent()

		return jdbcTemplate.query(
			sql,
			rowMapper,
			tableName,
			IngestStatus.NEW.toString(),
			IngestStatus.RETRY.toString(),
			offset,
			limit
		)
	}

	fun getFailedData(offset: Int = 0, limit: Int = 100): List<ArenaData> {
		val sql =
			"""
				SELECT * FROM $TABLE_NAME WHERE $FIELD_INGEST_STATUS = ?::arena_ingest_status ORDER BY $FIELD_OPERATION_POS ASC OFFSET ? LIMIT ?
			""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, IngestStatus.FAILED.toString(), offset, limit)
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

	fun markAsIgnored(id: Int) {
		val sql =
			"""
				UPDATE $TABLE_NAME SET $FIELD_INGEST_STATUS = ?::arena_ingest_status WHERE $FIELD_ID = ?
			""".trimIndent()

		jdbcTemplate.update(sql, IngestStatus.IGNORED.toString(), id)
	}

	fun setFailed(message: ArenaData, reason: String? = null, exception: Exception? = null) {
		if (reason != null) {
			if (exception != null) {
				logger.warn(reason, exception)
			} else {
				logger.warn(reason)
			}
		}

		markAsFailed(message.id)
	}

}
