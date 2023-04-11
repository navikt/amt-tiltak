package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class PublishRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		PublishDbo(
			id = rs.getUUID("id"),
			type = DataPublishType.valueOf(rs.getString("type")),
			hash = rs.getString("hash"),
			firstPublished = rs.getLocalDateTime("first_published"),
			lastPublished = rs.getLocalDateTime("last_published")
		)
	}

	fun set(id: UUID, type: DataPublishType, hash: String) {
		val sql = """
			INSERT INTO publish(id, type, hash)
			VALUES(:id, :type, :hash)
			ON CONFLICT (id, type) DO UPDATE SET hash = :hash,
				last_published = CURRENT_TIMESTAMP
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"type" to type,
			"hash" to hash
		)

		template.update(sql, parameters)
	}

	fun hasHash(id: UUID, type: DataPublishType, hash: String): Boolean {
		return get(id, type)?.hash == hash
	}

	private fun get(id: UUID, type: DataPublishType): PublishDbo? {
		val sql = """

		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"type" to type
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	private data class PublishDbo(
		val id: UUID,
		val type: DataPublishType,
		val hash: String,
		val firstPublished: LocalDateTime,
		val lastPublished: LocalDateTime
	)

}
