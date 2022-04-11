package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class AnsattRolleRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AnsattRolleDbo(
			id = rs.getUUID("id"),
			ansattId = rs.getUUID("ansatt_id"),
			arrangorId = rs.getUUID("arrangor_id"),
			rolle = AnsattRolle.valueOf(rs.getString("rolle")),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	internal fun opprettRolle(id: UUID, ansattId: UUID, arrangorId: UUID, rolle: AnsattRolle) {
		val sql = """
			INSERT INTO arrangor_ansatt_rolle(id, ansatt_id, arrangor_id, rolle)
				VALUES(:id, :ansattId, :arrangorId, CAST(:rolle AS arrangor_rolle))
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"ansattId" to ansattId,
			"arrangorId" to arrangorId,
			"rolle" to rolle.name
		)

		template.update(sql, parameters)
	}

	internal fun hentRoller(ansattId: UUID, arrangorId: UUID): List<AnsattRolleDbo> {
		val sql = """
			SELECT * FROM arrangor_ansatt_rolle WHERE ansatt_id = :ansattId AND arrangor_id = :arrangorId
		""".trimIndent()

		val parameters = sqlParameters(
			"ansattId" to ansattId,
			"arrangorId" to arrangorId,
		)

		return template.query(sql, parameters, rowMapper)
	}

	internal fun hentArrangorIderForAnsatt(ansattId: UUID): List<UUID> {
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"ansattId" to ansattId
		))

		return template.query(
			"SELECT arrangor_id FROM arrangor_ansatt_rolle WHERE ansatt_id = :ansattId",
			parameters
		) { rs, _ ->
			rs.getUUID("arrangor_id")
		}
	}

}
