package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime
import java.util.UUID

class IdQueries(
	private val template: NamedParameterJdbcTemplate
) {
	fun hentDeltakerIds(offset: Int, limit: Int, modifiedAfter: LocalDateTime): List<UUID> {
		val sql = """
        	SELECT id
			FROM deltaker
			WHERE modified_at >= :modified_after
			ORDER BY id
			OFFSET :offset LIMIT :limit
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters(
				"modified_after" to modifiedAfter,
				"offset" to offset,
				"limit" to limit
			)
		) { rs, _ -> rs.getUUID("id") }
	}

	fun hentEndringsmeldingIds(offset: Int, limit: Int, modifiedAfter: LocalDateTime): List<UUID> {
		val sql = """
			SELECT id
			FROM endringsmelding
			WHERE modified_at >= :modified_after
			ORDER BY id
			OFFSET :offset
			LIMIT :limit
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters(
				"modified_after" to modifiedAfter,
				"offset" to offset,
				"limit" to limit
			)
		) { rs, _ -> rs.getUUID("id") }
	}
}
