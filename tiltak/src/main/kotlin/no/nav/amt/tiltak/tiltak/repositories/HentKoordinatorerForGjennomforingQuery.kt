package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class HentKoordinatorerForGjennomforingQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		val fornavn: String = rs.getString("fornavn")
		val mellomnavn: String? = rs.getString("mellomnavn")
		val etternavn: String = rs.getString("etternavn")

		if (mellomnavn != null) {
			"$fornavn $mellomnavn $etternavn"
		} else {
			"$fornavn $etternavn"
		}

	}

	private val sql = """
		SELECT a.fornavn    AS fornavn,
			   a.mellomnavn AS mellomnavn,
			   a.etternavn  AS etternavn
		FROM arrangor_ansatt a
				 INNER JOIN arrangor_ansatt_rolle aar on a.id = aar.ansatt_id
				 INNER JOIN arrangor_ansatt_gjennomforing_tilgang aagt on aar.ansatt_id = aagt.ansatt_id
		WHERE aagt.gjennomforing_id = :gjennomforingId
		  AND aar.rolle = 'KOORDINATOR'
	""".trimIndent()

	fun query(gjennomforingId: UUID): List<String> {
		return template.query(
			sql,
			sqlParameters("gjennomforingId" to gjennomforingId),
			rowMapper
		)
	}

}
