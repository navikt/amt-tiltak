package no.nav.amt.tiltak.tiltak.metrics

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class GjennomforingMetricRepository(
	val template: NamedParameterJdbcTemplate
) {

	fun antallGjennomforingerPerType(): List<AntallGjennomforingerPerTypeMetric> {
		val sql = """
			select t.type, count(distinct g.id) as antall
			from tiltak t
				join gjennomforing g on t.id = g.tiltak_id
				join arrangor_ansatt_gjennomforing_tilgang aagt on g.id = aagt.gjennomforing_id
			where aagt.gyldig_til > current_timestamp
			group by  t.type;
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			AntallGjennomforingerPerTypeMetric(rs.getString("type"), rs.getInt("antall"))
		}
	}

	data class AntallGjennomforingerPerTypeMetric(
		val type: String,
		val antall: Int,
	)
}
