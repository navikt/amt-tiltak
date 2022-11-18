package no.nav.amt.tiltak.tiltak.metrics

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class GjennomforingMetricRepository(
	val template: NamedParameterJdbcTemplate
) {

	fun antallGjennomforingerPerType(): List<AntallGjennomforingerPerTypeMetric> {
		val sql = """
			SELECT t.type, COUNT(DISTINCT g.id) AS antall
			FROM tiltak t
				JOIN gjennomforing g ON t.id = g.tiltak_id
				JOIN arrangor_ansatt_gjennomforing_tilgang aagt ON g.id = aagt.gjennomforing_id
			WHERE aagt.gyldig_til > current_timestamp
			GROUP BY  t.type;
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			AntallGjennomforingerPerTypeMetric(rs.getString("type"), rs.getInt("antall"))
		}
	}

	fun antallGjennomforingerGruppert() = template.query(
		"""
			SELECT g.status AS status, aagt.id IS NOT NULL AS gjennomforing_med_bruker_hos_arrangor, COUNT(*) AS antall
			FROM gjennomforing g
			LEFT JOIN arrangor_ansatt_gjennomforing_tilgang aagt on g.id = aagt.gjennomforing_id
			GROUP BY g.status, gjennomforing_med_bruker_hos_arrangor;
		""".trimMargin()
	) { rs, _ ->
		GjennomforingMetrikker(
			rs.getString("status"),
			rs.getBoolean("gjennomforing_med_bruker_hos_arrangor"),
			rs.getInt("antall")
		)
	}
}

data class AntallGjennomforingerPerTypeMetric(
	val type: String,
	val antall: Int,
)

data class GjennomforingMetrikker(
	val status: String,
	val synligHosArrangor: Boolean,
	val antall: Int
)
