package no.nav.amt.tiltak.deltaker.repositories

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

typealias StatusStatistikk = Pair<String, Int>

@Component
class DeltakerStatistikkRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun antallDeltakere() = template.queryForObject(
		"SELECT count(*) FROM deltaker;",
		MapSqlParameterSource(),
		Int::class.java)

	fun antallDeltakerePerStatus() = template.query(
		"SELECT status, count(id) AS antall FROM deltaker_status WHERE aktiv = true GROUP BY status;"
	) {
			rs, _ -> StatusStatistikk(rs.getString("status"), rs.getInt("antall"))
	}

	fun antallArrangorer() = template.queryForObject(
		"SELECT count(*) FROM arrangor;",
		MapSqlParameterSource(),
		Int::class.java)

	fun antallArrangorerMedBrukere() = template.queryForObject(
		"SELECT count(distinct arrangor_id) FROM arrangor a join arrangor_ansatt_rolle aar on a.id = aar.arrangor_id;",
		MapSqlParameterSource(),
		Int::class.java)

	fun antallGjennomforinger() = template.queryForObject(
		"SELECT count(*) FROM gjennomforing;",
		MapSqlParameterSource(),
		Int::class.java)

	fun antallGjennomforingerPrStatus() = template.query(
		"SELECT status, count(*) as antall FROM gjennomforing GROUP BY status;"
	) {
			rs, _ -> StatusStatistikk(rs.getString("status"), rs.getInt("antall"))
	}

	fun eksponerteBrukere(): Int {
		val query = """
			SELECT count(distinct d.bruker_id) FROM
				gjennomforing g JOIN
				deltaker d ON d.gjennomforing_id = g.id
			WHERE g.arrangor_id IN (
				SELECT distinct arrangor_id FROM arrangor a join arrangor_ansatt_rolle aar on a.id = aar.arrangor_id
			);
		""".trimIndent()
		return template.queryForObject(query, MapSqlParameterSource(), Int::class.java)!!
	}

}


