package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.*

@Component
class HentKoordinatorerForGjennomforingQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		Pair(rs.getUUID("gjennomforingId"), getKoordinatorName(rs))
	}

	private val sql = """
		SELECT aagt.gjennomforing_id AS gjennomforingId,
			   a.fornavn    AS fornavn,
			   a.mellomnavn AS mellomnavn,
			   a.etternavn  AS etternavn
		FROM arrangor_ansatt a
				 INNER JOIN arrangor_ansatt_rolle aar on a.id = aar.ansatt_id
				 INNER JOIN arrangor_ansatt_gjennomforing_tilgang aagt on aar.ansatt_id = aagt.ansatt_id
		WHERE aagt.gjennomforing_id IN (:gjennomforingIds)
		  AND aar.rolle = 'KOORDINATOR'
		  AND aagt.gyldig_fra < CURRENT_TIMESTAMP
		  AND aagt.gyldig_til > CURRENT_TIMESTAMP
	""".trimIndent()

	fun query(gjennomforingId: UUID): List<String> {
		val koordinatorer = query(listOf(gjennomforingId))

		return koordinatorer[gjennomforingId]
			?: emptyList()
	}

	fun query(gjennomforingIds: List<UUID>): Map<UUID, List<String>> {
		val data = template.query(
			sql,
			sqlParameters("gjennomforingIds" to gjennomforingIds),
			rowMapper
		)

		val retData = mutableMapOf<UUID, List<String>>()

		gjennomforingIds.forEach { id ->
			val koordinatorerForGjennomforing = data
				.filter { it.first == id }
				.map { it.second }

			retData[id] = koordinatorerForGjennomforing
		}

		return retData
	}

	private fun getKoordinatorName(rs: ResultSet): String {
		val fornavn: String = rs.getString("fornavn")
		val mellomnavn: String? = rs.getString("mellomnavn")
		val etternavn: String = rs.getString("etternavn")

		return if (mellomnavn != null) {
			"$fornavn $mellomnavn $etternavn"
		} else {
			"$fornavn $etternavn"
		}
	}

}
