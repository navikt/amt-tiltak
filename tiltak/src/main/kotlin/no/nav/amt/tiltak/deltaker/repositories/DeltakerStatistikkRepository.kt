package no.nav.amt.tiltak.deltaker.repositories

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

typealias StatusStatistikk = Pair<String, Int>

data class GjennomforingMetrikker(
	val status: String,
	val synligHosArrangor: Boolean,
	val antall: Int
)

@Component
class DeltakerStatistikkRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun antallDeltakere() = template.queryForObject(
		"SELECT count(*) FROM deltaker;",
		MapSqlParameterSource(),
		Int::class.java
	)

	fun antallDeltakerePerStatus() = template.query(
		"SELECT status, count(id) AS antall FROM deltaker_status WHERE aktiv = true GROUP BY status;"
	) { rs, _ ->
		StatusStatistikk(rs.getString("status"), rs.getInt("antall"))
	}

	fun antallArrangorer() = template.queryForObject(
		"SELECT count(*) FROM arrangor;",
		MapSqlParameterSource(),
		Int::class.java
	)

	fun antallAktiveArrangorerMedBrukere() = template.queryForObject(
		"""
			SELECT count(distinct a.id)
			FROM arrangor a
			join arrangor_ansatt_rolle aar on a.id = aar.arrangor_id
			join gjennomforing g on a.id = g.arrangor_id
			where g.status = 'GJENNOMFORES';
		""".trimMargin(),
		MapSqlParameterSource(),
		Int::class.java
	)

	fun antallAktiveArrangorer() = template.queryForObject(
		"""
			SELECT count(distinct arrangor_id)
			FROM arrangor a
			join gjennomforing g on a.id = g.arrangor_id
			where g.status = 'GJENNOMFORES';
		""".trimMargin(),
		MapSqlParameterSource(),
		Int::class.java
	)

	fun antallArrangorerMedBrukere() = template.queryForObject(
		"SELECT count(distinct arrangor_id) FROM arrangor a join arrangor_ansatt_rolle aar on a.id = aar.arrangor_id;",
		MapSqlParameterSource(),
		Int::class.java
	)

	@Deprecated("Bør kunne erstattes med antallGjennomforingerMedGrupperinger")
	fun antallGjennomforinger() = template.queryForObject(
		"SELECT count(*) FROM gjennomforing;",
		MapSqlParameterSource(),
		Int::class.java
	)

	@Deprecated("Bør kunne erstattes med antallGjennomforingerMedGrupperinger")
	fun antallGjennomforingerPrStatus() = template.query(
		"SELECT status, count(*) as antall FROM gjennomforing GROUP BY status;"
	) { rs, _ ->
		StatusStatistikk(rs.getString("status"), rs.getInt("antall"))
	}

	fun antallGjennomforingerGruppert() = template.query(
		"""
			SELECT g.status as status, aagt.id is not null as gjennomforing_med_bruker_hos_arrangor, count(*) as antall
			FROM gjennomforing g
			left join arrangor_ansatt_gjennomforing_tilgang aagt on g.id = aagt.gjennomforing_id
			group by g.status, gjennomforing_med_bruker_hos_arrangor;
		""".trimMargin()
	) { rs, _ ->
		GjennomforingMetrikker(
			rs.getString("status"),
			rs.getBoolean("gjennomforing_med_bruker_hos_arrangor"),
			rs.getInt("antall")
		)
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

	fun eksponerteBrukerePrStatus(): List<StatusStatistikk> {
		val query = """
			select ds.status as status, count(distinct d.bruker_id) as antall
			from deltaker d
			inner join deltaker_status ds on d.id = ds.deltaker_id
			where d.gjennomforing_id in (
				select gjennomforing_id
				from arrangor_ansatt_gjennomforing_tilgang tilgang
				where d.gjennomforing_id = tilgang.gjennomforing_id)
			and ds.aktiv = true
			group by ds.status;
		""".trimIndent()
		return template.query(query) { rs, _ ->
			StatusStatistikk(rs.getString("status"), rs.getInt("antall"))
		}
	}

}


