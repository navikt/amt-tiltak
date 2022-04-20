package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class GjennomforingTilgangRepository(
	private val template: NamedParameterJdbcTemplate
) {

	internal fun opprettTilgang(id: UUID, ansattId: UUID, gjennomforingId: UUID) {
		val sql = """
			INSERT INTO arrangor_ansatt_gjennomforing_tilgang(id, ansatt_id, gjennomforing_id)
				VALUES(:id, :ansattId, :gjennomforingId)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"ansattId" to ansattId,
			"gjennomforingId" to gjennomforingId
		)

		template.update(sql, parameters)
	}

	internal fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return template.query(
			"SELECT gjennomforing_id FROM arrangor_ansatt_gjennomforing_tilgang WHERE ansatt_id = :ansattId",
			MapSqlParameterSource().addValue("ansattId", ansattId),
		) { rs, _ -> rs.getUUID("gjennomforing_id") }
	}

	internal fun hentGjennomforingerForAnsattHosArrangor(ansattId: UUID, arrangorId: UUID): List<UUID> {
		val sql = """
			SELECT gjennomforing_id
			FROM arrangor_ansatt_gjennomforing_tilgang
			 INNER JOIN gjennomforing g on g.id = gjennomforing_id
			 WHERE ansatt_id = :ansattId AND g.arrangor_id = :arrangorId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(mapOf(
			"ansattId" to ansattId,
			"arrangorId" to arrangorId
		))

		return template.query(
			sql,
			parameters,
		) { rs, _ -> rs.getUUID("gjennomforing_id") }
	}

}
