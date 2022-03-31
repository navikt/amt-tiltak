package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class GjennomforingTilgangRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun hentGjennomforingerForAnsatt(ansattId: UUID): List<UUID> {
		return template.query(
			"SELECT gjennomforing_id FROM arrangor_ansatt_gjennomforing_tilgang WHERE ansatt_id = :ansattId",
			MapSqlParameterSource().addValue("ansattId", ansattId),
		) { rs, _ -> rs.getUUID("gjennomforing_id") }
	}

	fun hentGjennomforingerForAnsattHosArrangor(ansattId: UUID, arrangorId: UUID): List<UUID> {
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
