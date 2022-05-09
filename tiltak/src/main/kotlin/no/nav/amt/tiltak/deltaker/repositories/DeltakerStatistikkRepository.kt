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

	fun antallAktiveArrangorerMedBrukere() = template.queryForObject(
		"""
			SELECT count(distinct a.id)
			FROM arrangor a
			join arrangor_ansatt_rolle aar on a.id = aar.arrangor_id
			join gjennomforing g on a.id = g.arrangor_id
			where g.status = 'GJENNOMFORES';
		""".trimMargin(),
		MapSqlParameterSource(),
		Int::class.java)

	fun antallAktiveArrangorer() = template.queryForObject(
		"""
			SELECT count(distinct arrangor_id)
			FROM arrangor a
			join gjennomforing g on a.id = g.arrangor_id
			where g.status = 'GJENNOMFORES';
		""".trimMargin(),
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
			select count(distinct bruker_id)
			from deltaker
			where deltaker.gjennomforing_id in (
				select gjennomforing_id
				from arrangor_ansatt_gjennomforing_tilgang tilgang
				where deltaker.gjennomforing_id = tilgang.gjennomforing_id);
		""".trimIndent()
		return template.queryForObject(query, MapSqlParameterSource(), Int::class.java)!!
	}

}


