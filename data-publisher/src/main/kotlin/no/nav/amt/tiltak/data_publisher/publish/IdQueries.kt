package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class IdQueries(
	private val template: NamedParameterJdbcTemplate
) {

	fun hentArrangorIds(offset: Int, limit: Int): List<UUID> {
		return template.query(
			"SELECT id FROM arrangor ORDER BY id OFFSET :offset LIMIT :limit",
			sqlParameters("offset" to offset, "limit" to limit)
		) { rs, _ -> rs.getUUID("id") }
	}

	fun hentArrangorAnsattIds(offset: Int, limit: Int): List<UUID> {
		return template.query(
			"SELECT id FROM arrangor_ansatt ORDER BY id OFFSET :offset LIMIT :limit",
			sqlParameters("offset" to offset, "limit" to limit)
		) { rs, _ -> rs.getUUID("id") }
	}

	fun hentDeltakerIds(offset: Int, limit: Int): List<UUID> {
		return template.query(
			"SELECT id FROM deltaker ORDER BY id OFFSET :offset LIMIT :limit",
			sqlParameters("offset" to offset, "limit" to limit)
		) { rs, _ -> rs.getUUID("id") }
	}

	fun hentDeltakerlisteIds(offset: Int, limit: Int): List<UUID> {
		return template.query(
			"SELECT id FROM gjennomforing ORDER BY id OFFSET :offset LIMIT :limit",
			sqlParameters("offset" to offset, "limit" to limit)
		) { rs, _ -> rs.getUUID("id") }
	}

	fun hentEndringsmeldingIds(offset: Int, limit: Int): List<UUID> {
		return template.query(
			"SELECT id FROM endringsmelding ORDER BY id OFFSET :offset LIMIT :limit",
			sqlParameters("offset" to offset, "limit" to limit)
		) { rs, _ -> rs.getUUID("id") }
	}
}
