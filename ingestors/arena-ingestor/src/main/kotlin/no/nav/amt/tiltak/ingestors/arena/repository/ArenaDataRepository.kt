package no.nav.amt.tiltak.ingestors.arena.repository

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.IngestStatus
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
internal open class ArenaDataRepository(
	private val namedJdbcTemplate: NamedParameterJdbcTemplate
) {

	val rowMapper =
		RowMapper { rs, _ ->
			ArenaData(
				id = rs.getInt("id"),
				tableName = rs.getString("table_name"),
				operationType = OperationType.valueOf(rs.getString("operation_type")),
				operationPosition = rs.getLong("operation_pos"),
				operationTimestamp = rs.getTimestamp("operation_timestamp").toLocalDateTime(),
				ingestStatus = IngestStatus.valueOf(rs.getString("ingest_status")),
				ingestedTimestamp = rs.getTimestamp("ingested_timestamp")?.toLocalDateTime(),
				ingestAttempts = rs.getInt("ingest_attempts"),
				lastRetry = rs.getTimestamp("last_retry")?.toLocalDateTime(),
				before = rs.getString("before"),
				after = rs.getString("after")
			)
		}

	fun upsert(arenaData: ArenaData) {
		val sql =
			"""
				INSERT INTO arena_data (table_name, operation_type, operation_pos, operation_timestamp, ingest_status, before, after)
				VALUES (:tableName,
						:operationType,
						:operationPosition,
						:operationTimestamp,
						:ingestStatus,
						:before::json,
						:after::json)
				ON CONFLICT(table_name, operation_type, operation_pos) DO UPDATE SET
						ingest_status = :ingestStatus,
					  	ingested_timestamp = :ingestedTimestamp,
						ingest_attempts    = :ingestAttempts,
						last_retry         = :lastRetry
			""".trimIndent()

		namedJdbcTemplate.update(
			sql,
			arenaData.asParameterSource()
		)
	}

	fun getUningestedData(tableName: String, offset: Int = 0, limit: Int = 100): List<ArenaData> {
		return getByIngestStatusIn(
			tableName,
			listOf(IngestStatus.NEW, IngestStatus.RETRY),
			offset,
			limit
		)
	}

	fun getFailedData(tableName: String, offset: Int = 0, limit: Int = 1000): List<ArenaData> {
		return getByIngestStatusIn(
			tableName,
			listOf(IngestStatus.FAILED),
			offset, limit
		)
	}

	fun getById(id: Int): ArenaData {
		return namedJdbcTemplate.query(
			"SELECT * FROM arena_data WHERE id = :id",
			MapSqlParameterSource().addValues(mapOf("id" to id)),
			rowMapper
		).first()
			?: throw IllegalArgumentException("id $id is not found in table")
	}

	fun getByIngestStatusIn(
		tableName: String,
		statuses: List<IngestStatus>,
		offset: Int = 0,
		limit: Int = 100
	): List<ArenaData> {
		val sql = """
			SELECT *
			FROM arena_data
			WHERE ingest_status IN (:ingestStatuses)
			AND table_name = :tableName
			ORDER BY operation_pos ASC
			OFFSET :offset LIMIT :limit
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"ingestStatuses" to statuses.map { it.name }.toSet(),
				"tableName" to tableName,
				"offset" to offset,
				"limit" to limit
			)
		)

		return namedJdbcTemplate.query(
			sql,
			parameters,
			rowMapper
		)
	}

	private fun ArenaData.asParameterSource() = MapSqlParameterSource().addValues(
		mapOf(
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
		)
	)

}
